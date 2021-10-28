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
package org.eclipse.sirius.web.lsp.description;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.sirius.web.annotations.Immutable;
import org.eclipse.sirius.web.annotations.PublicApi;
import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.annotations.graphql.GraphQLObjectType;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.representations.VariableManager;

/**
 * The root concept of the description for LSP-backed textual representations.
 *
 * @author flatombe
 */
@PublicApi
@Immutable
@GraphQLObjectType
public final class LspTextDescription implements IRepresentationDescription {

	public static final String LABEL = "label"; //$NON-NLS-1$

	private UUID id;

	private String label;

	private Predicate<VariableManager> canCreatePredicate;

	private Function<VariableManager, String> targetObjectIdProvider;

	private Function<VariableManager, Object> targetObjectProvider;

	private Function<Object, String> serializer;

	private String languageName;

	private LspTextDescription() {
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
	public String getLabel() {
		return this.label;
	}

	@Override
	public Predicate<VariableManager> getCanCreatePredicate() {
		return this.canCreatePredicate;
	}

	public Function<VariableManager, String> getTargetObjectIdProvider() {
		return this.targetObjectIdProvider;
	}

	public Function<VariableManager, Object> getTargetObjectProvider() {
		return this.targetObjectProvider;
	}

	public Function<Object, String> getSerializer() {
		return this.serializer;
	}

	public String getLanguageName() {
		return this.languageName;
	}

	public static Builder newLspTextDescription(UUID id) {
		return new Builder(id);
	}

	@Override
	public String toString() {
		String pattern = "{0} '{'id: {1}, label: {2}'}'"; //$NON-NLS-1$
		return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.label);
	}

	/**
	 * Builder used to create a {@link LspTextDescription}.
	 *
	 * @author flatombe
	 */
	@SuppressWarnings("checkstyle:HiddenField")
	public static final class Builder {
		private final UUID id;

		private String label;

		private Predicate<VariableManager> canCreatePredicate;

		private Function<VariableManager, String> targetObjectIdProvider;

		private Function<VariableManager, Object> targetObjectProvider;

		private Function<Object, String> serializer;

		private String languageName;

		private Builder(UUID id) {
			this.id = Objects.requireNonNull(id);
		}

		public Builder label(String label) {
			this.label = Objects.requireNonNull(label);
			return this;
		}

		public Builder canCreatePredicate(Predicate<VariableManager> canCreatePredicate) {
			this.canCreatePredicate = Objects.requireNonNull(canCreatePredicate);
			return this;
		}

		public Builder targetObjectIdProvider(Function<VariableManager, String> targetObjectIdProvider) {
			this.targetObjectIdProvider = Objects.requireNonNull(targetObjectIdProvider);
			return this;
		}

		public Builder targetObjectProvider(Function<VariableManager, Object> targetObjectProvider) {
			this.targetObjectProvider = Objects.requireNonNull(targetObjectProvider);
			return this;
		}

		public Builder serializer(Function<Object, String> serializer) {
			this.serializer = Objects.requireNonNull(serializer);
			return this;
		}

		public Builder languageName(String languageName) {
			this.languageName = Objects.requireNonNull(languageName);
			return this;
		}

		public LspTextDescription build() {
			LspTextDescription lspTextDescription = new LspTextDescription();
			lspTextDescription.id = Objects.requireNonNull(this.id);
			lspTextDescription.label = Objects.requireNonNull(this.label);
			lspTextDescription.canCreatePredicate = Objects.requireNonNull(this.canCreatePredicate);
			lspTextDescription.targetObjectIdProvider = Objects.requireNonNull(this.targetObjectIdProvider);
			lspTextDescription.targetObjectProvider = Objects.requireNonNull(this.targetObjectProvider);
			lspTextDescription.serializer = Objects.requireNonNull(this.serializer);
			lspTextDescription.languageName = Objects.requireNonNull(this.languageName);
			return lspTextDescription;
		}
	}
}
