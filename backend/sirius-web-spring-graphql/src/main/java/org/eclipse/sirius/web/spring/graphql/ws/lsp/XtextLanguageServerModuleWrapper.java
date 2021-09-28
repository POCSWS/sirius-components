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

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.sirius.web.dsl.StaticRegistryProvider;
import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineResourceValidator;
import org.eclipse.xtext.ISetup;
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
public class XtextLanguageServerModuleWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(XtextLanguageServerModuleWrapper.class);

    private final ServerModule serverModule = new ServerModule() {
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
            this.bind(IResourceServiceProvider.Registry.class).toProvider(StaticRegistryProvider.class).asEagerSingleton();
        }
    };

    private final Injector serverModuleInjector = Guice.createInjector(this.serverModule);

    public XtextLanguageServerModuleWrapper() {
        // This instantiates a ServerModule whose CustomResourceServiceProviderServiceLoader reflexively instantiates
        // all found Guice Modules.
    }

    /**
     * Provides the {@link Injector} corresponding to the {@link ServerModule Xtext Language Server Guice module}.
     *
     * @return the (non-{@code null}) {@link Injector} of the {@link ServerModule}.
     */
    public Injector getServerModuleInjector() {
        return this.serverModuleInjector;
    }

    public Optional<Injector> getSetupInjector(Class<? extends ISetup> setupClass) {
        return Optional.ofNullable(this.getServerModuleInjector().getInstance(StaticRegistryProvider.class).getInjector(setupClass));
    }
}
