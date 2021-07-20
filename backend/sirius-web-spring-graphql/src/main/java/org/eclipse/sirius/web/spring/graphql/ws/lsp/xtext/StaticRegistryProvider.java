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
package org.eclipse.sirius.web.spring.graphql.ws.lsp.xtext;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineSiriusWebIdeSetup;
import org.eclipse.sirius.web.dsl.sysmlv2.xtext.SysMLSiriusWebIdeSetup;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;

/**
 * Workaround for ultimately allowing us to inject our {@link StatemachineResourceValidator}.
 *
 * @author flatombe
 */
@Singleton
public class StaticRegistryProvider implements Provider<Registry> {

    private final List<ISetup> setups = new ArrayList<>();

    /**
     * We need this map to remember all the Guice modules instantiated reflexively by this registry, so we can retrieve
     * some of their instances later on.
     */
    private final Map<Class<? extends ISetup>, Injector> injectorsBySetupType = new LinkedHashMap<>();

    private final Registry registry = new ResourceServiceProviderRegistryImpl();

    public StaticRegistryProvider() {
        // Default implementation in Xtext-LS finds all setups reflexively
        // In our case, we want to explicitly list all the setups to consider.
        this.setups.add(new StatemachineSiriusWebIdeSetup());
        this.setups.add(new SysMLSiriusWebIdeSetup());
        this.fillRegistry();
    }

    private void fillRegistry() {
        for (ISetup setup : this.setups) {
            Injector injector = setup.createInjectorAndDoEMFRegistration();
            IResourceServiceProvider resourceServiceProvider = injector.getInstance(IResourceServiceProvider.class);
            FileExtensionProvider extensionProvider = injector.getInstance(FileExtensionProvider.class);
            String primaryFileExtension = extensionProvider.getPrimaryFileExtension();
            for (String fileExtension : extensionProvider.getFileExtensions()) {
                if (this.registry.getExtensionToFactoryMap().containsKey(fileExtension)) {
                    if (primaryFileExtension.equals(fileExtension))
                        this.registry.getExtensionToFactoryMap().put(fileExtension, resourceServiceProvider);
                } else {
                    this.registry.getExtensionToFactoryMap().put(fileExtension, resourceServiceProvider);
                }
            }
            this.injectorsBySetupType.put(setup.getClass(), injector);
        }
    }

    public Injector getInjector(Class<? extends ISetup> setupClass) {
        return this.injectorsBySetupType.get(setupClass);
    }

    @Override
    public IResourceServiceProvider.Registry get() {
        return this.registry;
    }
}
