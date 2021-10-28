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
package org.eclipse.sirius.web.dsl.sysmlv2.xtext;

import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.util.Modules2;
import org.omg.sysml.xtext.SysMLStandaloneSetupGenerated;
import org.omg.sysml.xtext.ide.SysMLIdeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
public class SysMLSiriusWebIdeSetup extends SysMLStandaloneSetupGenerated {

	public static Injector doSetup() {
		return new SysMLSiriusWebIdeSetup().createInjectorAndDoEMFRegistration();
	}

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new SysMLSiriusWebRuntimeModule(), new SysMLIdeModule()));
	}
}
