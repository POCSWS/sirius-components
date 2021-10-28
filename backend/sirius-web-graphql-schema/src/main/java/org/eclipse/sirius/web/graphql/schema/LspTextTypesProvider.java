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
package org.eclipse.sirius.web.graphql.schema;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sirius.web.graphql.utils.providers.GraphQLObjectTypeProvider;
import org.eclipse.sirius.web.graphql.utils.schema.ITypeProvider;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.springframework.stereotype.Service;

import graphql.schema.GraphQLType;

/**
 * {@link ITypeProvider} implementation providing the GraphQL types related to {@link LspText} representations.
 *
 * @author flatombe
 */
@Service
public class LspTextTypesProvider implements ITypeProvider {
    public static final String LSPTEXT_TYPE = "LspText"; //$NON-NLS-1$

    private final GraphQLObjectTypeProvider graphQLObjectTypeProvider = new GraphQLObjectTypeProvider();

    @Override
    public Set<GraphQLType> getTypes() {
        // @formatter:off
        List<Class<?>> objectClasses = List.of(
            LspText.class,
            LspTextDescription.class
        );

        return objectClasses.stream()
            .map(this.graphQLObjectTypeProvider::getType)
            .collect(Collectors.toUnmodifiableSet());
        // @formatter:on

    }

}
