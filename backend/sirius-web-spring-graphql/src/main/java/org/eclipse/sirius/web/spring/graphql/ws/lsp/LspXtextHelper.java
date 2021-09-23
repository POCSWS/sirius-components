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

import com.google.inject.Guice;
import com.google.inject.Injector;

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
import org.eclipse.sirius.web.dsl.statemachine.xtext.CustomResourceServiceProviderServiceLoader;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator;
import org.eclipse.xtext.ide.ExecutorServiceProvider;
import org.eclipse.xtext.ide.server.DefaultProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.IMultiRootWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.IProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.MultiRootWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.ServerModule;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.ProjectDescriptionBasedContainerManager;
import org.eclipse.xtext.validation.IResourceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for starting the Xtext Language Server.
 *
 * TODO: I believe this LS implementation is supposed to support all Xtext-defined DSLs but we should confirm this by
 * trying having two DSLs on our classpath.
 *
 * @author flatombe
 */
public class LspXtextHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LspXtextHelper.class);

    private final Injector injector = Guice.createInjector(new ServerModule() {
        @Override
        protected void configure() {
            // Copied from super implementation
            this.binder().bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
            this.bind(IMultiRootWorkspaceConfigFactory.class).to(MultiRootWorkspaceConfigFactory.class);
            this.bind(IProjectDescriptionFactory.class).to(DefaultProjectDescriptionFactory.class);
            this.bind(IContainer.Manager.class).to(ProjectDescriptionBasedContainerManager.class);

            // Binding our own custom implementations.
            this.bind(LanguageServer.class).to(SiriusWebLanguageServerImpl.class);
            this.bind(IResourceValidator.class).to(StatemachineResourceValidator.class);
            this.bind(IResourceServiceProvider.Registry.class).toProvider(CustomResourceServiceProviderServiceLoader.class).asEagerSingleton();
        }
    });

    public LspXtextHelper() {
    }

    public Injector getInjector() {
        return this.injector;
    }

    /**
     * Creates and starts the Xtext Language Server.
     *
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
    public LanguageServer startXtextLanguageServer(String languageServerId, InputStream languageServerIn, OutputStream languageServerOut) {
        Objects.requireNonNull(languageServerId);
        Objects.requireNonNull(languageServerIn);
        Objects.requireNonNull(languageServerOut);

        final SiriusWebLanguageServerImpl xtextLanguageServer = this.getInjector().getInstance(SiriusWebLanguageServerImpl.class);

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Function<MessageConsumer, MessageConsumer> wrapper = consumer -> consumer;

        final Launcher<LanguageClient> languageServerLauncher = new Launcher.Builder<LanguageClient>().setLocalService(xtextLanguageServer).setRemoteInterface(LanguageClient.class)
                .setInput(languageServerIn).setOutput(languageServerOut).setExecutorService(executorService).wrapMessages(wrapper).create();

        xtextLanguageServer.connect(languageServerLauncher.getRemoteProxy());
        LOGGER.info(String.format("[%s]Starting Xtext Language Server...", languageServerId)); //$NON-NLS-1$

        final Future<Future<?>> languageServerStarting = executorService.submit(() -> {
            final Future<?> languageServerListening = languageServerLauncher.startListening();
            LOGGER.info(String.format("[%s]Xtext Language Server started successfully", languageServerId)); //$NON-NLS-1$
            while (!languageServerListening.isDone()) {
                // Listen until the Language Server InputStream gets closed.
            }
            if (languageServerListening.isDone()) {
                LOGGER.info(String.format("[%s]Shutting down Xtext Language Server...", languageServerId)); //$NON-NLS-1$
                xtextLanguageServer.shutdown();
                LOGGER.info(String.format("[%s]Xtext Language Server shut down successfully", languageServerId)); //$NON-NLS-1$
            }
            return languageServerListening;
        });

        return xtextLanguageServer;
    }
}
