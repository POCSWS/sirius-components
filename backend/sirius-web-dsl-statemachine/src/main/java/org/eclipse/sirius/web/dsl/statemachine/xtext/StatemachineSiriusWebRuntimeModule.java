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

import org.eclipse.sirius.web.xtext.ResourceValidatorImplWithPostValidation;
import org.eclipse.xtext.validation.IResourceValidator;

import fr.obeo.dsl.designer.sample.StatemachineRuntimeModule;

/**
 * Specialization of {@link StatemachineRuntimeModule} to bind our
 * {@link IResourceValidator} implementation.
 * 
 * @author flatombe
 *
 */
public class StatemachineSiriusWebRuntimeModule extends StatemachineRuntimeModule {

	public StatemachineSiriusWebRuntimeModule() {
		super();
	}

	/**
	 * This binding gets picked up reflexively thanks to the method name.
	 */
	public Class<? extends IResourceValidator> bindIResourceValidator() {
		return ResourceValidatorImplWithPostValidation.class;
	}
}
