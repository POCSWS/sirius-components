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
package org.eclipse.sirius.web.dsl.statemachine.xtext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.resource.ResourceServiceProviderServiceLoader;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;

import com.google.inject.Injector;

import fr.obeo.dsl.designer.sample.ide.StatemachineIdeSetup;

/**
 * Workaround for allowing us to inject our
 * {@link StatemachineResourceValidator}.
 *
 * @author flatombe
 */
public class ResourceServiceProviderServiceLoaderWithOverride extends ResourceServiceProviderServiceLoader {
	private final ServiceLoader<ISetup> setupLoader = ServiceLoader.load(ISetup.class);

	/**
	 * We need this map to remember all the Guice modules instantiated reflexively
	 * by this registry, so we can retrieve some of their instances later on.
	 */
	private final Map<Class<? extends ISetup>, Injector> setupInjectors = new LinkedHashMap<>();

	private final Registry loadedRegistry = this.loadRegistry();

	public ResourceServiceProviderServiceLoaderWithOverride() {
	}

	private Registry loadRegistry() {
		ResourceServiceProviderRegistryImpl registry = new ResourceServiceProviderRegistryImpl();
		for (ISetup cp : this.setupLoader) {
			if (cp instanceof StatemachineIdeSetup) {
				// FIXME: this is a workaround.
				// ServiceLoader relies on introspection of the classloader to find ISetup
				// instances.
				// It will find StatemachineIdSetup, which is bound with the default
				// IResourceValidator implementation
				// (ResourceValidatorImpl).
				// But we want our own implementation to be used (while also not having to tweak
				// the Xtext jar).
				// In the future, either setup a template of how a Sirius Web-compatible Xtext
				// Setup could be generated,
				// or improve how we perform this workaround.
				// CHECKSTYLE:OFF
				cp = new StatemachineSiriusWebIdeSetup();
				// CHECKSTYLE:ON
			}
			Injector injector = cp.createInjectorAndDoEMFRegistration();
			IResourceServiceProvider resourceServiceProvider = injector.getInstance(IResourceServiceProvider.class);
			FileExtensionProvider extensionProvider = injector.getInstance(FileExtensionProvider.class);
			String primaryFileExtension = extensionProvider.getPrimaryFileExtension();
			for (String ext : extensionProvider.getFileExtensions()) {
				if (registry.getExtensionToFactoryMap().containsKey(ext)) {
					if (primaryFileExtension.equals(ext))
						registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
				} else {
					registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
				}
			}

			// TODO: this is a hack to hopefully have access to the right instance of our
			// custom IResourceValidator.
			setupInjectors.put(cp.getClass(), injector);
		}
		return registry;
	}

	public Injector getInjector(Class<? extends ISetup> setupClass) {
		return this.setupInjectors.get(setupClass);
	}

	@Override
	public IResourceServiceProvider.Registry get() {
		return this.loadedRegistry;
	}
}
