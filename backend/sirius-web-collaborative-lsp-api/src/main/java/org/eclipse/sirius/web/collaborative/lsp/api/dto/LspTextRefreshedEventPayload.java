/*******************************************************************************
 * Copyright (c) 2019, 2021 Obeo.
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
package org.eclipse.sirius.web.collaborative.lsp.api.dto;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.annotations.graphql.GraphQLObjectType;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.lsp.LspText;

/**
 * Payload used to indicate that the lspText has been refreshed.
 *
 * @author sbegaudeau
 */
@GraphQLObjectType
public final class LspTextRefreshedEventPayload implements IPayload {
    private final UUID id;

    private final LspText lspText;

    public LspTextRefreshedEventPayload(UUID id, LspText lspText) {
        this.id = Objects.requireNonNull(id);
        this.lspText = Objects.requireNonNull(lspText);
    }

    @Override
    @GraphQLID
    @GraphQLField
    @GraphQLNonNull
    public UUID getId() {
        return this.id;
    }

    @GraphQLField
    @GraphQLNonNull
    public LspText getLspText() {
        return this.lspText;
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}, lspText: '{'id: {2}, label: {3}'}''}'"; //$NON-NLS-1$
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.lspText.getId(), this.lspText.getLabel());
    }
}
