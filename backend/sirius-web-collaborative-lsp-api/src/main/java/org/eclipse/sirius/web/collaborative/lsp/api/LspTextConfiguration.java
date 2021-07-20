/*******************************************************************************
 * Copyright (c) 2019, 2020 Obeo.
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

import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.collaborative.api.services.IRepresentationConfiguration;

/**
 * The configuration used to create a lspText event processor.
 *
 * @author sbegaudeau
 */
public class LspTextConfiguration implements IRepresentationConfiguration {

    private final UUID lspTextId;

    public LspTextConfiguration(UUID lspTextId) {
        this.lspTextId = Objects.requireNonNull(lspTextId);
    }

    @Override
    public UUID getId() {
        return this.lspTextId;
    }

}
