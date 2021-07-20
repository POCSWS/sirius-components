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
package org.eclipse.sirius.web.spring.collaborative.lsp;

import java.util.Objects;

import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextContext;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.events.ILspTextEvent;

/**
 * The implementation of {@link ILspTextContext}.
 *
 * @author hmarchadour
 */
public class LspTextContext implements ILspTextContext {

    private LspText lspText;

    private ILspTextEvent lspTextEvent;

    public LspTextContext(LspText initialLspText) {
        this.lspText = Objects.requireNonNull(initialLspText);
    }

    @Override
    public LspText getLspText() {
        return this.lspText;
    }

    @Override
    public void update(LspText mutateLspText) {
        this.lspText = Objects.requireNonNull(mutateLspText);
    }

    @Override
    public ILspTextEvent getLspTextEvent() {
        return this.lspTextEvent;
    }

    @Override
    public void setLspTextEvent(ILspTextEvent lspTextEvent) {
        this.lspTextEvent = lspTextEvent;
    }

    @Override
    public void reset() {
        this.lspTextEvent = null;
    }
}
