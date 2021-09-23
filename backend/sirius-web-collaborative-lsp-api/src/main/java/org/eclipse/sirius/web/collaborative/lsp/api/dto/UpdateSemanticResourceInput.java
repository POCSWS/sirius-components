/*******************************************************************************
 * Copyright (c) 2021 THALES GLOBAL SERVICES.
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

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.web.annotations.graphql.GraphQLField;
import org.eclipse.sirius.web.annotations.graphql.GraphQLID;
import org.eclipse.sirius.web.annotations.graphql.GraphQLNonNull;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextInput;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.lsp.LspText;

/**
 * The {@link IInput} implementation for the mutation where we update a semantic resource because it was edited through
 * an {@link LspText} representation.
 *
 * @author flatombe
 */
public final class UpdateSemanticResourceInput implements ILspTextInput {

    private UUID id;

    private final Resource resource;

    private final UUID representationId;

    public UpdateSemanticResourceInput(Resource resource, UUID representationId) {
        this.id = UUID.randomUUID();
        this.resource = Objects.requireNonNull(resource);
        this.representationId = Objects.requireNonNull(representationId);
    }

    @Override
    @GraphQLID
    @GraphQLField
    @GraphQLNonNull
    public UUID getId() {
        return this.id;
    }

    /**
     * The new version of the semantic {@link Resource} to update.
     *
     * @return the (non-{@code null}) semantic {@link Resource} resulting from parsing the {@link LspText}
     *         representation.
     */
    @GraphQLField
    @GraphQLNonNull
    public Resource getResource() {
        return this.resource;
    }

    /**
     * The {@link UUID} of the representation.
     *
     * @return the (non-{@code null}) {@link UUID} of the representation that triggered this change.
     */
    @Override
    @GraphQLField
    @GraphQLNonNull
    public UUID getRepresentationId() {
        return this.representationId;
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}, resource: {2}, representationId: {3}'}'"; //$NON-NLS-1$
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.id, this.resource, this.representationId);
    }
}
