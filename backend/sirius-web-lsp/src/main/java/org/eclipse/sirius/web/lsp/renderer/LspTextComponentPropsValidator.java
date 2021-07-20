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

import org.eclipse.sirius.web.components.IComponentPropsValidator;
import org.eclipse.sirius.web.components.IProps;
import org.eclipse.sirius.web.lsp.components.LspTextComponent;
import org.eclipse.sirius.web.lsp.components.LspTextComponentProps;

/**
 * {@link IComponentPropsValidator} implementation for validating the props of
 * {@link LspTextComponent}.
 *
 * @author flatombe
 */
public class LspTextComponentPropsValidator implements IComponentPropsValidator {

	@Override
	public boolean validateComponentProps(Class<?> componentType, IProps props) {
		boolean checkValidProps = false;

		if (LspTextComponent.class.equals(componentType)) {
			checkValidProps = props instanceof LspTextComponentProps;
		}

		return checkValidProps;
	}

}
