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
package org.eclipse.sirius.web.spring.collaborative.lsp.dto;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLInputObjectType;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextInput;

/**
 * The input of the rename lspText mutation.
 *
 * @author pcdavid
 */
@GraphQLInputObjectType
public final class RenameLspTextInput implements ILspTextInput {

    private UUID id;

    private UUID editingContextId;

    private UUID lspTextId;

    private String newLabel;

    public RenameLspTextInput() {
        // Used by Jackson
    }

    public RenameLspTextInput(UUID id, UUID editingContextId, UUID lspTextId, String newLabel) {
        this.id = Objects.requireNonNull(id);
        this.editingContextId = Objects.requireNonNull(editingContextId);
        this.lspTextId = Objects.requireNonNull(lspTextId);
        this.newLabel = Objects.requireNonNull(newLabel);
    }

    @Override
    @GraphQLID
    @GraphQLField
    @GraphQLNonNull
    public UUID getId() {
        return this.id;
    }

    @GraphQLID
    @GraphQLField
    @GraphQLNonNull
    public UUID getEditingContextId() {
        return this.editingContextId;
    }

    @Override
    @GraphQLID
    @GraphQLField
    @GraphQLNonNull
    public UUID getRepresentationId() {
        return this.lspTextId;
    }

    @GraphQLField
    @GraphQLNonNull
    public String getNewLabel() {
        return this.newLabel;
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}, editingContextId: {2}, lspTextId: {3}, newLabel: {4}'}'"; //$NON-NLS-1$
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.editingContextId, this.lspTextId, this.newLabel);
    }
}
