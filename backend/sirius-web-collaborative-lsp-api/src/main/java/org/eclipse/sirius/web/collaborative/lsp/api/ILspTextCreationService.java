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
package org.eclipse.sirius.web.collaborative.lsp.api;

import java.util.Optional;

import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;

/**
 * Service to create or refresh {@link LspText} representations.
 *
 * @author flatombe
 */
public interface ILspTextCreationService {

    /**
     * Creates a new {@link LspText} using the given parameters.
     *
     * @param label
     *            The label of the LspText
     * @param targetObject
     *            The object used as the target
     * @param diagramDescription
     *            The description of the LspText
     * @param editingContext
     *            The editing context
     * @return A new LspText saved in the data store
     */
    LspText create(String label, Object targetObject, LspTextDescription lspTextDescription, IEditingContext editingContext);

    /**
     * Refresh an existing LspText.
     *
     * @param editingContext
     *            The editing context
     * @param lspTextContext
     *            The LspText context
     * @return An updated LspText if we have been able to refresh it.
     */
    Optional<LspText> refresh(IEditingContext editingContext, ILspTextContext lspTextContext);

}
