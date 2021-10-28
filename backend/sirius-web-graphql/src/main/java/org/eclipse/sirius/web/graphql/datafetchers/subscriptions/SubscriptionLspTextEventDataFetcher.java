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
package org.eclipse.sirius.web.graphql.datafetchers.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import org.eclipse.sirius.web.annotations.graphql.GraphQLSubscriptionTypes;
import org.eclipse.sirius.web.annotations.spring.graphql.SubscriptionDataFetcher;
import org.eclipse.sirius.web.collaborative.api.dto.SubscribersUpdatedEventPayload;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextEventProcessor;
import org.eclipse.sirius.web.collaborative.lsp.api.LspTextConfiguration;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.LspTextEventInput;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.LspTextRefreshedEventPayload;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.graphql.schema.SubscriptionTypeProvider;
import org.eclipse.sirius.web.spring.graphql.api.IDataFetcherWithFieldCoordinates;
import org.reactivestreams.Publisher;

import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;

/**
 * The data fetcher used to send the refreshed lspText to a subscription.
 * <p>
 * It will used to fetch the data from the following GraphQL field:
 * </p>
 *
 * <pre>
 * type Subscription {
 *   lspTextEvent(input: LspTextEventInput): LspTextEventPayload
 * }
 * </pre>
 *
 * @author flatombe
 */
//@formatter:off
@GraphQLSubscriptionTypes(
    input = LspTextEventInput.class,
    payloads = {
        LspTextRefreshedEventPayload.class,
        SubscribersUpdatedEventPayload.class,
    }
)
@SubscriptionDataFetcher(type = SubscriptionTypeProvider.TYPE, field = SubscriptionLspTextEventDataFetcher.LSPTEXT_EVENT_FIELD)
//@formatter:on
public class SubscriptionLspTextEventDataFetcher implements IDataFetcherWithFieldCoordinates<Publisher<IPayload>> {

    public static final String LSPTEXT_EVENT_FIELD = "lspTextEvent"; //$NON-NLS-1$

    private final ObjectMapper objectMapper;

    private final IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    public SubscriptionLspTextEventDataFetcher(ObjectMapper objectMapper, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.editingContextEventProcessorRegistry = Objects.requireNonNull(editingContextEventProcessorRegistry);
    }

    @Override
    public Publisher<IPayload> get(DataFetchingEnvironment environment) throws Exception {
        Object argument = environment.getArgument(SubscriptionTypeProvider.INPUT_ARGUMENT);
        var input = this.objectMapper.convertValue(argument, LspTextEventInput.class);
        var lspTextConfiguration = new LspTextConfiguration(input.getLspTextId());

        // @formatter:off
        return this.editingContextEventProcessorRegistry.getOrCreateEditingContextEventProcessor(input.getEditingContextId())
                .flatMap(processor -> processor.acquireRepresentationEventProcessor(ILspTextEventProcessor.class, lspTextConfiguration, input))
                .map(representationEventProcessor -> representationEventProcessor.getOutputEvents(input))
                .orElse(Flux.empty());
        // @formatter:on
    }

}
