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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceInput;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator.PostResourceValidationBehavior;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineSiriusWebIdeSetup;
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

    private final PostResourceValidationBehavior postValidationBehavior;

    private final WebSocketSession session;

    private final StatemachineResourceValidator resourceValidator;

    private final Future<Future<Void>> runningLauncher;

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
            LanguageServerRuntime.this.logger.debug("[{}]Response: {}", LanguageServerRuntime.this.session.getId(), responsePayload); //$NON-NLS-1$
            if (LanguageServerRuntime.this.session.isOpen()) {
                try {
                    LanguageServerRuntime.this.session.sendMessage(new TextMessage(responsePayload));
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            } else {
                LanguageServerRuntime.this.logger.error("[{}]WebSocket session has been closed so the response cannot be sent", LanguageServerRuntime.this.session.getId()); //$NON-NLS-1$
            }
        }
    };

    /**
     * Creates and starts a new {@link LanguageServerRuntime}.
     *
     * @param webSocketSession
     *            the (non-{@code null}) {@link WebSocketSession}. It is expected that all messages received for this
     *            session are headered and forwarded to {@link #getWebSocketOut()}.
     * @param editingContextEventProcessorRegistry
     *            the (non-{@code null}) {@link IEditingContextEventProcessorRegistry}.
     */
    public LanguageServerRuntime(WebSocketSession webSocketSession, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry) {
        this.session = Objects.requireNonNull(webSocketSession);
        Objects.requireNonNull(editingContextEventProcessorRegistry);

        this.logger.debug("[{}]Instantiating LSRuntime", this.session.getId()); //$NON-NLS-1$

        final UUID editingContextId = (UUID) this.session.getAttributes().get("editingContextId"); //$NON-NLS-1$
        final UUID representationId = (UUID) this.session.getAttributes().get("representationId"); //$NON-NLS-1$

        try {
            this.languageServerIn.connect(this.webSocketOut);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }

        final XtextLanguageServerModuleWrapper xtextLanguageServerModuleWrapper = new XtextLanguageServerModuleWrapper();

        this.postValidationBehavior = (issues, resource) -> {
            Principal principal = this.session.getPrincipal();
            if (principal instanceof Authentication) {
                SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
            }

            if (issues.isEmpty()) {
                // When the textual representation parses into a valid model, we want to update the semantic model (and
                // also other representations).
                final IInput input = new UpdateSemanticResourceInput(resource, representationId);
                this.logger.debug("Validation successful, dispatching event " + input.toString()); //$NON-NLS-1$
                Optional<IPayload> maybeEventResult = editingContextEventProcessorRegistry.dispatchEvent(editingContextId, input);
                this.logger.debug("Event dispatch resulting payload: " + maybeEventResult.map(Object::toString)); //$NON-NLS-1$
            }
        };
        this.resourceValidator = xtextLanguageServerModuleWrapper.getSetupInjector(StatemachineSiriusWebIdeSetup.class).orElseThrow().getInstance(StatemachineResourceValidator.class);
        this.resourceValidator.addPostValidationBehavior(this.postValidationBehavior);

        this.logger.debug("[{}]Creating and starting Xtext Language Server...", this.session.getId()); //$NON-NLS-1$
        final SiriusWebLanguageServerImpl languageServer = xtextLanguageServerModuleWrapper.getServerModuleInjector().getInstance(SiriusWebLanguageServerImpl.class);
        Launcher<LanguageClient> launcher = createJsonRpcLauncher(languageServer, this.languageServerIn, this.languageServerOut);
        this.runningLauncher = this.start(launcher, Executors.newCachedThreadPool());
        this.logger.debug("[{}]Started Xtext Language Server", this.session.getId()); //$NON-NLS-1$
    }

    /**
     * Creates the {@link Launcher} necessary for starting a {@link LanguageServer}.
     *
     * @param languageServer
     *            the (non-{@code null}) {@link SiriusWebLanguageServerImpl} that is the local service of the launcher..
     * @param languageServerIn
     *            the (non-{@code null}) {@link InputStream} on which the Xtext Language Server will read JSON-RPC
     *            messages which are LSP client requests. Closing this stream will shutdown the language server.
     * @param languageServerOut
     *            the (non-{@code null}) {@link OutputStream} on which the Xtext Language Server will write JSON-RPC
     *            messages which are LSP responses.
     */
    private static Launcher<LanguageClient> createJsonRpcLauncher(SiriusWebLanguageServerImpl languageServer, InputStream languageServerIn, OutputStream languageServerOut) {
        Objects.requireNonNull(languageServer);
        Objects.requireNonNull(languageServerIn);
        Objects.requireNonNull(languageServerOut);

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Function<MessageConsumer, MessageConsumer> wrapper = consumer -> consumer;

        final Launcher<LanguageClient> launcher = new Launcher.Builder<LanguageClient>().setLocalService(languageServer).setRemoteInterface(LanguageClient.class).setInput(languageServerIn)
                .setOutput(languageServerOut).setExecutorService(executorService).wrapMessages(wrapper).create();
        languageServer.connect(launcher.getRemoteProxy());

        return launcher;
    }

    /**
     * Starts a {@link Launcher} in an {@link ExecutorService}.
     *
     * @param languageServerId
     *            the (non-{@code null}) {@link String} to uniquely identify this language server (in case we end up
     *            having to run several for some reason).
     * @param launcher
     *            the (non-{@code null}) {@link Launcher} to run.
     * @param executorService
     *            the (non-{@code null}) {@link ExecutorService} in which {@code launcher} will be ran.
     */
    private Future<Future<Void>> start(Launcher<LanguageClient> launcher, ExecutorService executorService) {
        final Future<Future<Void>> languageServerStarting = executorService.submit(() -> {
            this.logger.trace("[{}]JSON-RPC Launcher started successfully", this.session.getId()); //$NON-NLS-1$
            final Future<Void> languageServerListening = launcher.startListening();
            while (!languageServerListening.isDone()) {
                // Listen until the Language Server InputStream gets closed.
            }
            return languageServerListening;
        });
        return languageServerStarting;
    }

    /**
     * Forwards a {@link TextMessage} to this {@link LanguageServerRuntime}.
     *
     * @param message
     *            the (non-{@code null}) {@link TextMessage}.
     */
    public void forwardMessage(TextMessage message) {
        this.logger.debug(String.format("[%s]Received message of size %s:%s", this.session.getId(), message.getPayloadLength(), message.getPayload())); //$NON-NLS-1$
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
        this.logger.debug("[{}]Shutting down LSRuntime...", this.session.getId()); //$NON-NLS-1$

        try {
            this.languageServerIn.close();
            this.languageServerOut.close();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } finally {
            this.resourceValidator.removePostValidationBehavior(this.postValidationBehavior);
        }

        this.logger.trace("[{}]Shutting down Xtext Language Server...", this.session.getId()); //$NON-NLS-1$
        if (this.runningLauncher.cancel(true)) {
            // try {
            // final boolean cancelled = this.runningLauncher.get().cancel(true);
            // if (cancelled) {
            this.logger.trace("[{}]JSON-RPC Launcher shut down successfully", this.session.getId()); //$NON-NLS-1$
            // } else {
            // logger.info("[{}]JSON-RPC Launcher could not be shut down", this.session.getId()); //$NON-NLS-1$
            // }
            // } catch (InterruptedException | ExecutionException exception) {
            // throw new RuntimeException(exception);
            // }
        }
        this.logger.trace("[{}]Shut down Xtext Language Server successfully", this.session.getId()); //$NON-NLS-1$
        this.logger.debug("[{}]Shut down LSRuntime", this.session.getId()); //$NON-NLS-1$
    }
}
