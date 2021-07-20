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
package org.eclipse.sirius.web.lsp.components;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.web.components.Element;
import org.eclipse.sirius.web.components.IComponent;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.eclipse.sirius.web.lsp.elements.LspTextElementProps;
import org.eclipse.sirius.web.representations.VariableManager;

/**
 * The {@link IComponent} implementation used to render an {@link LspText}.
 *
 * @author flatombe
 */
public class LspTextComponent implements IComponent {

	private final LspTextComponentProps props;

	public LspTextComponent(LspTextComponentProps props) {
		this.props = props;
	}

	@Override
	public Element render() {
		VariableManager variableManager = this.props.getVariableManager();
		LspTextDescription lspTextDescription = this.props.getLspTextDescription();
		String label = lspTextDescription.getLabel();

		String targetObjectId = lspTextDescription.getTargetObjectIdProvider().apply(variableManager);

		// FLA: Diagrams are re-created and we want to keep the same ID
		// But for now I am not sure how this will happen for LspText...
		Optional<LspText> maybePreviousLspText = this.props.getPreviousLspText();
		UUID lspTextId = maybePreviousLspText.map(LspText::getId).orElseGet(UUID::randomUUID);

		Object targetObject = lspTextDescription.getTargetObjectProvider().apply(variableManager);
		final String contents = lspTextDescription.getSerializer().apply(targetObject);

		// @formatter:off
		LspTextElementProps lspTextElementProps = LspTextElementProps.newLspTextElementProps(lspTextId)
				.label(label)
				.descriptionId(lspTextDescription.getId())
				.targetObjectId(targetObjectId)
				.children(new ArrayList<>())
				.languageName(lspTextDescription.getLanguageName())
				.contents(contents)
				.build();
        // @formatter:on

		return new Element(LspTextElementProps.TYPE, lspTextElementProps);
	}
}
