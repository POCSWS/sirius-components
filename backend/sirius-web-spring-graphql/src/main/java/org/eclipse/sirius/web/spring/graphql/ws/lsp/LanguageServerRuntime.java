/*******************************************************************************
 * Copyright (c) 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.spring.graphql.ws.lsp;

import com.google.inject.Injector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceInput;
import org.eclipse.sirius.web.dsl.statemachine.xtext.CustomResourceServiceProviderServiceLoader;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator.PostResourceValidationBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Represents a {@link LanguageServer} implementation that is connected to a {@link WebSocketSession}. Messages received
 * via the WebSocket are sent as requests to the server and responses provided by the server are sent as
 * {@link WebSocketSession#sendMessage(org.springframework.web.socket.WebSocketMessage) WebSocketMessages}.
 *
 * @author flatombe
 */
public class LanguageServerRuntime {
    private final Logger logger = LoggerFactory.getLogger(LanguageServerRuntime.class);

    private final Collection<Consumer<String>> responseListeners = new LinkedHashSet<>();

    private PostResourceValidationBehavior postValidationBehavior;

    // TODO: for now we launch a Language Server per WebSocketSession.
    private final WebSocketSession session;

    private final LspXtextHelper lspXtextHelper;

    // Messages received by the WebSocket will be accessible via this stream.
    private final PipedOutputStream webSocketOut = new PipedOutputStream();

    // The Language Server will read its input from this stream.
    private final PipedInputStream languageServerIn = new PipedInputStream();

    // The Language Server will provide responses through this stream.
    private final OutputStream languageServerOut = new ByteArrayOutputStream() {
        @Override
        public synchronized void flush() throws IOException {
            super.flush();

            // Everytime the server flushes, we forward the response to the frontend through the WebSocket.
            this.forwardResponse();
            this.reset();
        }

        private void forwardResponse() {
            String responsePayload = new String(this.toByteArray(), StandardCharsets.UTF_8);
            int i = responsePayload.indexOf("\r\n\r\n"); //$NON-NLS-1$
            if (i > 0) {
                responsePayload = responsePayload.substring(i + 4);
            }
            LanguageServerRuntime.this.logger.info(String.format("[%s]Response: %s", LanguageServerRuntime.this.session.getId(), responsePayload)); //$NON-NLS-1$
            try {
                LanguageServerRuntime.this.session.sendMessage(new TextMessage(responsePayload));
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        }
    };

    /**
     * Creates and starts a new {@link LanguageServerRuntime}.
     *
     * @param webSocketSession
     *            the (non-{@code null}) {@link WebSocketSession}. It is expected that all messages received for this
     *            session are headered and forwarded to {@link #getWebSocketOut()}.
     */
    public LanguageServerRuntime(WebSocketSession webSocketSession, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry) {
        final String path = webSocketSession.getUri().getPath();
        final String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        final UUID editingContextId = UUID.fromString(lastSegment);

        this.session = Objects.requireNonNull(webSocketSession);
        Objects.requireNonNull(editingContextEventProcessorRegistry);
        try {
            this.languageServerIn.connect(this.webSocketOut);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        this.lspXtextHelper = new LspXtextHelper();
        LanguageServer xtextLanguageServer = this.lspXtextHelper.startXtextLanguageServer(this.session.getId(), this.languageServerIn, this.languageServerOut);

        // With this build listener, we will have a chance to react after every build.
        // What we want to do is if the build was successful, then update the model contents.
        // languageServer.addCustomBuildListener(new
        // XtextLanguageServerBuildListenerForUpdatingSemanticModel(webSocketSession,
        // editingContextEventProcessorRegistry));
        this.postValidationBehavior = (issues, resource) -> {
            Principal principal = this.session.getPrincipal();
            if (principal instanceof Authentication) {
                SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
            }

            if (issues.isEmpty()) {
                editingContextEventProcessorRegistry.dispatchEvent(editingContextId, new UpdateSemanticResourceInput(resource));
            }
        };
        final Injector dslInjector = this.lspXtextHelper.getInjector().getInstance(CustomResourceServiceProviderServiceLoader.class).getInjector("StatemachineSiriusWebIdeSetup"); //$NON-NLS-1$
        dslInjector.getInstance(StatemachineResourceValidator.class).addPostValidationBehavior(this.postValidationBehavior);
    }

    /**
     * Forwards a {@link TextMessage} to this {@link LanguageServerRuntime}.
     *
     * @param message
     *            the (non-{@code null}) {@link TextMessage}.
     */
    public void forwardMessage(TextMessage message) {
        this.logger.info(String.format("[%s]Received message of size %s:%s", this.session.getId(), message.getPayloadLength(), message.getPayload())); //$NON-NLS-1$
        try {
            // Header part, cf.
            // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#headerPart
            this.webSocketOut.write(("Content-Length: " + message.getPayloadLength() + "\r\n").getBytes(StandardCharsets.US_ASCII)); //$NON-NLS-1$ //$NON-NLS-2$
            this.webSocketOut.write("\r\n".getBytes(StandardCharsets.US_ASCII)); //$NON-NLS-1$

            // Content part, cf.
            // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#contentPart
            this.webSocketOut.write(message.getPayload().getBytes(StandardCharsets.UTF_8));

            // This will trigger the language server to read the messages we just wrote.
            this.webSocketOut.flush();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    /**
     * Shuts down this {@link LanguageServerRuntime}.
     */
    public void shutdown() {
        try {
            this.languageServerIn.close();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } finally {
            final Injector dslInjector = this.lspXtextHelper.getInjector().getInstance(CustomResourceServiceProviderServiceLoader.class).getInjector("StatemachineSiriusWebIdeSetup"); //$NON-NLS-1$
            dslInjector.getInstance(StatemachineResourceValidator.class).removePostValidationBehavior(this.postValidationBehavior);
        }
    }
}
