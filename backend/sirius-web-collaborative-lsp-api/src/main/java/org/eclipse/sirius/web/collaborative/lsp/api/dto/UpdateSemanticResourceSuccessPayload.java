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
package org.eclipse.sirius.web.collaborative.lsp.api.dto;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.annotations.graphql.GraphQLObjectType;
import org.eclipse.sirius.web.core.api.IPayload;

/**
 * The {@link IPayload} resulting from successfully handling an {@link UpdateSemanticResourceInput}.
 *
 * @author flatombe
 */
@GraphQLObjectType
public final class UpdateSemanticResourceSuccessPayload implements IPayload {

    private final UUID id;

    private final String editingContextId;

    private final String updatedSemanticResourceId;

    public UpdateSemanticResourceSuccessPayload(UUID id, String editingContextId, String updatedSemanticResourceId) {
        this.id = Objects.requireNonNull(id);
        this.editingContextId = Objects.requireNonNull(editingContextId);
        this.updatedSemanticResourceId = Objects.requireNonNull(updatedSemanticResourceId);
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
    public String getEditingContextId() {
        return this.editingContextId;
    }

    @GraphQLField
    @GraphQLNonNull
    public String getUpdatedSemanticResourceId() {
        return this.updatedSemanticResourceId;
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}, editingContextId: {2}, updatedSemanticResourceId: {3}'}'"; //$NON-NLS-1$
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.editingContextId, this.updatedSemanticResourceId);
    }
}
