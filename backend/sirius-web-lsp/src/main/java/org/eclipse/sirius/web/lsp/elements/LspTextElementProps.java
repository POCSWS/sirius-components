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
package org.eclipse.sirius.web.lsp.elements;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.annotations.Immutable;
import org.eclipse.sirius.web.components.Element;
import org.eclipse.sirius.web.components.IProps;

/**
 * The properties of the form element.
 *
 * @author flatombe
 */
@Immutable
public final class LspTextElementProps implements IProps {
	public static final String TYPE = "Text with LSP"; //$NON-NLS-1$

	private UUID id;

	private String label;

	private String targetObjectId;

	private UUID descriptionId;

	private String contents;

	// FLA: leaving this here for now just in case, but not sure we will need it.
	private List<Element> children;

	private LspTextElementProps() {
		// Prevent instantiation
	}

	public UUID getId() {
		return this.id;
	}

	public String getLabel() {
		return this.label;
	}

	public String getTargetObjectId() {
		return this.targetObjectId;
	}

	public UUID getDescriptionId() {
		return this.descriptionId;
	}

	public String getContents() {
		return this.contents;
	}

	public static Builder newLspTextElementProps(UUID id) {
		return new Builder(id);
	}

	@Override
	public String toString() {
		String pattern = "{0} '{'id: {1}, label: {2}', contents: {3}}'"; //$NON-NLS-1$
		return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.label, this.contents);
	}

	/**
	 * The builder of the form element props.
	 *
	 * @author sbegaudeau
	 */
	@SuppressWarnings("checkstyle:HiddenField")
	public static final class Builder {
		private UUID id;

		private String label;

		private String targetObjectId;

		private UUID descriptionId;

		private String contents;

		private List<Element> children;

		private Builder(UUID id) {
			this.id = Objects.requireNonNull(id);
		}

		public Builder label(String label) {
			this.label = Objects.requireNonNull(label);
			return this;
		}

		public Builder targetObjectId(String targetObjectId) {
			this.targetObjectId = Objects.requireNonNull(targetObjectId);
			return this;
		}

		public Builder descriptionId(UUID descriptionId) {
			this.descriptionId = Objects.requireNonNull(descriptionId);
			return this;
		}

		public Builder contents(String contents) {
			this.contents = Objects.requireNonNull(contents);
			return this;
		}

		public Builder children(List<Element> children) {
			this.children = Objects.requireNonNull(children);
			return this;
		}

		public LspTextElementProps build() {
			LspTextElementProps lspTextElementProps = new LspTextElementProps();
			lspTextElementProps.id = Objects.requireNonNull(this.id);
			lspTextElementProps.label = Objects.requireNonNull(this.label);
			lspTextElementProps.targetObjectId = Objects.requireNonNull(this.targetObjectId);
			lspTextElementProps.descriptionId = Objects.requireNonNull(this.descriptionId);
			lspTextElementProps.contents = Objects.requireNonNull(this.contents);
			lspTextElementProps.children = Objects.requireNonNull(this.children);
			return lspTextElementProps;
		}
	}
}
