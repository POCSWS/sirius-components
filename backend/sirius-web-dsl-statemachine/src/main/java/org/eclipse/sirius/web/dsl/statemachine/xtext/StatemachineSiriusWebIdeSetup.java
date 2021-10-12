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

import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.util.Modules2;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.obeo.dsl.designer.sample.StatemachineStandaloneSetupGenerated;
import fr.obeo.dsl.designer.sample.ide.StatemachineIdeModule;

/**
 * {@link ISetup} implementation for the Sirius Web compatible version of the
 * Statemachine DSL.
 * 
 * TODO: I found also StatemachineIdeSetup which is not used but it does not
 * seems to me like LSP-based implementations make use of those bindings.
 * 
 * @author flatombe
 *
 */
public class StatemachineSiriusWebIdeSetup extends StatemachineStandaloneSetupGenerated {

	public static Injector doSetup() {
		return new StatemachineSiriusWebIdeSetup().createInjectorAndDoEMFRegistration();
	}

	@Override
	public Injector createInjector() {
		return Guice
				.createInjector(Modules2.mixin(new StatemachineSiriusWebRuntimeModule(), new StatemachineIdeModule()));
	}
}
