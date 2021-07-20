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
package org.eclipse.sirius.web.dsl.statemachine.configuration;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sirius.web.emf.services.ILabelFeatureProvider;
import org.eclipse.sirius.web.emf.services.LabelFeatureProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.obeo.dsl.designer.sample.statemachine.StatemachinePackage;
import fr.obeo.dsl.designer.sample.statemachine.provider.StatemachineItemProviderAdapterFactory;

/**
 * {@link Configuration} for the {@code Statemachine} domain (DSL example
 * provided by Xtext).
 *
 * @author flatombe
 */
@Configuration
public class StatemachineConfiguration {

	@Bean
	public AdapterFactory statemachineAdapterFactory() {
		return new StatemachineItemProviderAdapterFactory();
	}

	@Bean
	public EPackage statemachineEPackage() {
		return StatemachinePackage.eINSTANCE;
	}

	@Bean
	public ILabelFeatureProvider statemachineLabelFeatureProvider() {
		return new LabelFeatureProvider(StatemachinePackage.eINSTANCE.getNsURI(), new StatemachineLabelFeatureSwitch(),
				new StatemachineEditableSwitch());
	}
}
