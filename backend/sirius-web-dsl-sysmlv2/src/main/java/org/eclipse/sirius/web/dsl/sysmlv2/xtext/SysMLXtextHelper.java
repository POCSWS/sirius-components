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

import com.google.inject.Injector;

/**
 * Centralizes the initialization of the Xtext setup for the Statemachine DSL in
 * a Sirius Web context.
 * 
 * @author flatombe
 *
 */
public class SysMLXtextHelper {

	private static final Injector INJECTOR = SysMLSiriusWebIdeSetup.doSetup();

	public static Injector getInjector() {
		return INJECTOR;
	}
}
