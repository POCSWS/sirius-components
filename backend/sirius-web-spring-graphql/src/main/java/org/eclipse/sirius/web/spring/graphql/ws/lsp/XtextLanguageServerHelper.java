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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for manipulating the APIs around the lifecycle of an Xtext Language Server.
 *
 * @author flatombe
 */
public class XtextLanguageServerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(XtextLanguageServerModuleWrapper.class);

    /**
     * Creates and starts the Xtext Language Server.
     *
     * @param languageServer
     *            the (non-{@code null}) {@link SiriusWebLanguageServerImpl} to start.
     * @param languageServerId
     *            the (non-{@code null}) {@link String} to uniquely identify this language server (in case we end up
     *            having to run several for some reason).
     * @param languageServerIn
     *            the (non-{@code null}) {@link InputStream} on which the Xtext Language Server will read JSON-RPC
     *            messages which are LSP client requests. Closing this stream will shutdown the language server.
     * @param languageServerOut
     *            the (non-{@code null}) {@link OutputStream} on which the Xtext Language Server will write JSON-RPC
     *            messages which are LSP responses.
     */
    public static LanguageServer startXtextLanguageServer(SiriusWebLanguageServerImpl languageServer, String languageServerId, InputStream languageServerIn, OutputStream languageServerOut) {
        Objects.requireNonNull(languageServer);
        Objects.requireNonNull(languageServerId);
        Objects.requireNonNull(languageServerIn);
        Objects.requireNonNull(languageServerOut);

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Function<MessageConsumer, MessageConsumer> wrapper = consumer -> consumer;

        final Launcher<LanguageClient> languageServerLauncher = new Launcher.Builder<LanguageClient>().setLocalService(languageServer).setRemoteInterface(LanguageClient.class)
                .setInput(languageServerIn).setOutput(languageServerOut).setExecutorService(executorService).wrapMessages(wrapper).create();

        languageServer.connect(languageServerLauncher.getRemoteProxy());
        LOGGER.info(String.format("[%s]Starting Xtext Language Server...", languageServerId)); //$NON-NLS-1$

        final Future<Future<?>> languageServerStarting = executorService.submit(() -> {
            final Future<?> languageServerListening = languageServerLauncher.startListening();
            LOGGER.info(String.format("[%s]Xtext Language Server started successfully", languageServerId)); //$NON-NLS-1$
            while (!languageServerListening.isDone()) {
                // Listen until the Language Server InputStream gets closed.
            }
            if (languageServerListening.isDone()) {
                LOGGER.info(String.format("[%s]Shutting down Xtext Language Server...", languageServerId)); //$NON-NLS-1$
                languageServer.shutdown();
                LOGGER.info(String.format("[%s]Xtext Language Server shut down successfully", languageServerId)); //$NON-NLS-1$
            }
            return languageServerListening;
        });

        return languageServer;
    }
}
