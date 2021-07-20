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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.web.components.IProps;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.eclipse.sirius.web.representations.VariableManager;

/**
 * The {@link IProps} implementation for {@link LspTextComponent}.
 *
 * @author flatombe
 */
public final class LspTextComponentProps implements IProps {
	private VariableManager variableManager;

	private LspTextDescription lspTextDescription;

	private Optional<LspText> previousLspText;

	private LspTextComponentProps() {
		// Prevent instantiation
	}

	public VariableManager getVariableManager() {
		return this.variableManager;
	}

	public LspTextDescription getLspTextDescription() {
		return this.lspTextDescription;
	}

	public Optional<LspText> getPreviousLspText() {
		return this.previousLspText;
	}

	public static Builder newLspTextComponentProps() {
		return new Builder();
	}

	/**
	 * The Builder to create a new {@link LspTextComponentProps}.
	 *
	 * @author fbarbin
	 */
	@SuppressWarnings("checkstyle:HiddenField")
	public static final class Builder {
		private VariableManager variableManager;

		private LspTextDescription lspTextDescription;

		private Optional<LspText> previousLspText;

		public Builder variableManager(VariableManager variableManager) {
			this.variableManager = Objects.requireNonNull(variableManager);
			return this;
		}

		public Builder lspTextDescription(LspTextDescription lspTextDescription) {
			this.lspTextDescription = Objects.requireNonNull(lspTextDescription);
			return this;
		}

		public Builder previousLspText(Optional<LspText> previousLspText) {
			this.previousLspText = Objects.requireNonNull(previousLspText);
			return this;
		}

		public LspTextComponentProps build() {
			LspTextComponentProps lspTextComponentProps = new LspTextComponentProps();
			lspTextComponentProps.variableManager = Objects.requireNonNull(this.variableManager);
			lspTextComponentProps.lspTextDescription = Objects.requireNonNull(this.lspTextDescription);
			lspTextComponentProps.previousLspText = Objects.requireNonNull(this.previousLspText);
			return lspTextComponentProps;
		}
	}

}
