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
package org.eclipse.sirius.web.lsp;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.annotations.Immutable;
import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.annotations.graphql.GraphQLObjectType;
import org.eclipse.sirius.web.representations.IRepresentation;
import org.eclipse.sirius.web.representations.ISemanticRepresentation;

/**
 * Root concept of the LSP-backed textual representation.
 *
 * @author flatombe
 */
@Immutable
@GraphQLObjectType
public final class LspText implements IRepresentation, ISemanticRepresentation {

	public static final String KIND = "Text with LSP"; //$NON-NLS-1$

	private UUID id;

	private UUID descriptionId;

	private String kind;

	private String label;

	private String targetObjectId;

	private String contents;

	private String languageName;

	private LspText() {
		// Prevent instantiation
	}

	@Override
	@GraphQLID
	@GraphQLField
	@GraphQLNonNull
	public UUID getId() {
		return this.id;
	}

	@Override
	@GraphQLField
	@GraphQLNonNull
	public String getKind() {
		return this.kind;
	}

	@Override
	@GraphQLNonNull
	@GraphQLField
	public String getLabel() {
		return this.label;
	}

	@Override
	@GraphQLID
	@GraphQLField
	@GraphQLNonNull
	public String getTargetObjectId() {
		return this.targetObjectId;
	}

	@Override
	@GraphQLID
	@GraphQLField
	@GraphQLNonNull
	public UUID getDescriptionId() {
		return this.descriptionId;
	}

	@GraphQLNonNull
	@GraphQLField
	public String getContents() {
		return this.contents;
	}

	@GraphQLNonNull
	@GraphQLField
	public String getLanguageName() {
		return this.languageName;
	}

	public static Builder newLspText(UUID id) {
		return new Builder(id);
	}

	@Override
	public String toString() {
		String pattern = "{0} '{'id: {1}, label: {2}, languageName: {3}, targetObjectId: {4}, descriptionId: {5}, contents: {6}'}'"; //$NON-NLS-1$
		return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.label, this.languageName,
				this.targetObjectId, this.descriptionId, this.contents);
	}

	/**
	 * The builder used to create an {@link LspText}.
	 *
	 * @author flatombe
	 */
	@SuppressWarnings("checkstyle:HiddenField")
	public static final class Builder {
		private UUID id;

		private String kind = KIND;

		private String label;

		private String targetObjectId;

		private UUID descriptionId;

		private String contents;

		private String languageName;

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

		public Builder languageName(String languageName) {
			this.languageName = Objects.requireNonNull(languageName);
			return this;
		}

		public LspText build() {
			LspText lspText = new LspText();
			lspText.id = Objects.requireNonNull(this.id);
			lspText.kind = Objects.requireNonNull(this.kind);
			lspText.label = Objects.requireNonNull(this.label);
			lspText.targetObjectId = Objects.requireNonNull(this.targetObjectId);
			lspText.descriptionId = Objects.requireNonNull(this.descriptionId);
			lspText.contents = Objects.requireNonNull(this.contents);
			lspText.languageName = Objects.requireNonNull(this.languageName);
			return lspText;
		}
	}
}
