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

import java.util.List;

import org.eclipse.sirius.web.components.IElementFactory;
import org.eclipse.sirius.web.components.IProps;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.elements.LspTextElementProps;

/**
 * {@link IElementFactory} implementation for instantiating {@link LspText}
 * elements.
 *
 * @author flatombe
 */
public class LspTextElementFactory implements IElementFactory {

	@Override
	public Object instantiateElement(String type, IProps props, List<Object> children) {
		Object object = null;
		if (LspTextElementProps.TYPE.equals(type) && props instanceof LspTextElementProps) {
			object = this.instantiateLspTextElement((LspTextElementProps) props, children);
		}
		return object;
	}

	private LspText instantiateLspTextElement(LspTextElementProps props, List<Object> children) {
		// @formatter:off
        return LspText.newLspText(props.getId())
                .label(props.getLabel())
                .targetObjectId(props.getTargetObjectId())
                .descriptionId(props.getDescriptionId())
                .contents(props.getContents())
                .build();
        // @formatter:on
	}

}
