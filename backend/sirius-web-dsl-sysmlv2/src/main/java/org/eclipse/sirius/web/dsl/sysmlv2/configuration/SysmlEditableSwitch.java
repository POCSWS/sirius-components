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

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.util.SysMLSwitch;

/**
 * {@link SysMLSwitch} implementation providing whether the label of an
 * {@link EObject} from the {@code SysML v2} domain is editable or not.
 *
 * @author flatombe
 *
 */
class SysmlEditableSwitch extends SysMLSwitch<Boolean> {

	@Override
	public Boolean defaultCase(EObject object) {
		return true;
	}
}
