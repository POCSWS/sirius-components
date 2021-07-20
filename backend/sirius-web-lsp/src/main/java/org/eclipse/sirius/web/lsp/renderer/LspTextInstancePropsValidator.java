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
package org.eclipse.sirius.web.lsp.renderer;

import org.eclipse.sirius.web.components.IInstancePropsValidator;
import org.eclipse.sirius.web.components.IProps;
import org.eclipse.sirius.web.lsp.elements.LspTextElementProps;

/**
 * {@link IInstancePropsValidator} implementation for validating the
 * {@link LspTextElementProps}.
 *
 * @author flatombe
 */
public class LspTextInstancePropsValidator implements IInstancePropsValidator {

	@Override
	public boolean validateInstanceProps(String type, IProps props) {
		boolean checkValidProps = false;

		if (LspTextElementProps.TYPE.equals(type)) {
			checkValidProps = props instanceof LspTextElementProps;
		}

		return checkValidProps;
	}

}
