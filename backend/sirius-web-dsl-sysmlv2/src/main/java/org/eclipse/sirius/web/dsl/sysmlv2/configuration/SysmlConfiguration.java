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
package org.eclipse.sirius.web.dsl.sysmlv2.configuration;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sirius.web.emf.services.ILabelFeatureProvider;
import org.eclipse.sirius.web.emf.services.LabelFeatureProvider;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.lang.sysml.provider.SysMLItemProviderAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} for the {@code SysML v2} language.
 *
 * @author flatombe
 */
@Configuration
public class SysmlConfiguration {
 
	@Bean
	public AdapterFactory sysmlAdapterFactory() {
		return new SysMLItemProviderAdapterFactory();
	}

	@Bean
	public EPackage sysmlEPackage() {
		return SysMLPackage.eINSTANCE;
	}

	@Bean
	public ILabelFeatureProvider sysmlLabelFeatureProvider() {
		return new LabelFeatureProvider(SysMLPackage.eINSTANCE.getNsURI(), new SysmlLabelFeatureSwitch(),
				new SysmlEditableSwitch());
	}
}
