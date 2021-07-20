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

import java.util.Optional;

import org.eclipse.sirius.web.components.BaseRenderer;
import org.eclipse.sirius.web.components.Element;
import org.eclipse.sirius.web.lsp.LspText;

/**
 * Renderer used to create the form from its description and some variables.
 * <p>
 * It will delegate most of its behavior to the abstract renderer which will
 * process the tree of elements to render. The form renderer will mostly be used
 * in order to let the abstract renderer delegate some text-specific behavior
 * such as the instantiation of the text concrete types and the validation of
 * the properties of both the text elements and the text components.
 * </p>
 *
 * @author flatombe
 */
public class LspTextRenderer {

	private final BaseRenderer baseRenderer;

	public LspTextRenderer() {
		this.baseRenderer = new BaseRenderer(new LspTextInstancePropsValidator(), new LspTextComponentPropsValidator(),
				new LspTextElementFactory());
	}

	public LspText render(Element element) {
		// @formatter:off
        return Optional.of(this.baseRenderer.renderElement(element))
                .filter(LspText.class::isInstance)
                .map(LspText.class::cast)
                .orElse(null);
        // @formatter:on
	}

}
