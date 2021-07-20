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

import org.eclipse.sirius.web.xtext.IResourceValidatorWithPostValidation;
import org.eclipse.sirius.web.xtext.ResourceValidatorImplWithPostValidation;
import org.eclipse.xtext.validation.IResourceValidator;
import org.omg.sysml.xtext.SysMLRuntimeModule;

/**
 * Specialization of {@link SysMLRuntimeModule} to bind our
 * {@link IResourceValidatorWithPostValidation} implementation.
 * 
 * @author flatombe
 *
 */
public class SysMLSiriusWebRuntimeModule extends SysMLRuntimeModule {

	public SysMLSiriusWebRuntimeModule() {
		super();
	}

	/**
	 * This binding gets picked up reflexively thanks to the method name.
	 */
	public Class<? extends IResourceValidator> bindIResourceValidator() {
		return ResourceValidatorImplWithPostValidation.class;
	}
}
