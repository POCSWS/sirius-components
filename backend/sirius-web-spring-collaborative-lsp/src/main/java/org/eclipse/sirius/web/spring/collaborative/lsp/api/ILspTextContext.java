/*******************************************************************************
 * Copyright (c) 2019, 2021 Obeo and others.
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
package org.eclipse.sirius.web.spring.collaborative.lsp.api;

import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.events.ILspTextEvent;

/**
 * Information used to perform some operations on the lspText.
 *
 * @author sbegaudeau
 */
public interface ILspTextContext {

    /**
     * The name of the variable used to store and retrieve the lspText context from a variable manager.
     */
    String LSPTEXT_CONTEXT = "lspTextContext"; //$NON-NLS-1$

    LspText getLspText();

    void update(LspText updatedLspText);

    void reset();

    ILspTextEvent getLspTextEvent();

    void setLspTextEvent(ILspTextEvent lspTextElementEvent);

}
