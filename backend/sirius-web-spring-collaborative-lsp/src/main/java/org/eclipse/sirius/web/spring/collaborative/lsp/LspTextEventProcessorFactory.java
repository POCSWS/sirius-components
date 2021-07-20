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
package org.eclipse.sirius.web.spring.collaborative.lsp;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.web.collaborative.api.services.IRepresentationConfiguration;
import org.eclipse.sirius.web.collaborative.api.services.IRepresentationEventProcessor;
import org.eclipse.sirius.web.collaborative.api.services.IRepresentationEventProcessorFactory;
import org.eclipse.sirius.web.collaborative.api.services.IRepresentationSearchService;
import org.eclipse.sirius.web.collaborative.api.services.ISubscriptionManagerFactory;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextCreationService;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextEventHandler;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextEventProcessor;
import org.eclipse.sirius.web.collaborative.lsp.api.LspTextConfiguration;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.lsp.LspText;
import org.springframework.stereotype.Service;

/**
 * {@link IRepresentationEventProcessorFactory} implementation to create {@link LspTextEventProcessor} instances.
 *
 * @author flatombe
 */
@Service
public class LspTextEventProcessorFactory implements IRepresentationEventProcessorFactory {
    private final IRepresentationSearchService representationSearchService;

    private final ILspTextCreationService lspTextCreationService;

    private final List<ILspTextEventHandler> lspTextEventHandlers;

    private final ISubscriptionManagerFactory subscriptionManagerFactory;

    public LspTextEventProcessorFactory(IRepresentationSearchService representationSearchService, ILspTextCreationService lspTextCreationService, List<ILspTextEventHandler> lspTextEventHandlers,
            ISubscriptionManagerFactory subscriptionManagerFactory) {
        this.representationSearchService = Objects.requireNonNull(representationSearchService);
        this.lspTextCreationService = Objects.requireNonNull(lspTextCreationService);
        this.lspTextEventHandlers = Objects.requireNonNull(lspTextEventHandlers);
        this.subscriptionManagerFactory = Objects.requireNonNull(subscriptionManagerFactory);
    }

    @Override
    public <T extends IRepresentationEventProcessor> boolean canHandle(Class<T> representationEventProcessorClass, IRepresentationConfiguration configuration) {
        return ILspTextEventProcessor.class.isAssignableFrom(representationEventProcessorClass) && configuration instanceof LspTextConfiguration;
    }

    @Override
    public <T extends IRepresentationEventProcessor> Optional<T> createRepresentationEventProcessor(Class<T> representationEventProcessorClass, IRepresentationConfiguration configuration,
            IEditingContext editingContext) {
        if (ILspTextEventProcessor.class.isAssignableFrom(representationEventProcessorClass) && configuration instanceof LspTextConfiguration) {
            LspTextConfiguration lspTextConfiguration = (LspTextConfiguration) configuration;
            var optionalLspText = this.representationSearchService.findById(lspTextConfiguration.getId(), LspText.class);
            if (optionalLspText.isPresent()) {
                LspText lspText = optionalLspText.get();

                // @formatter:off
                LspTextContext lspTextContext = new LspTextContext(lspText);
                IRepresentationEventProcessor lspTextEventProcessor = new LspTextEventProcessor(editingContext, lspTextContext,
                        this.lspTextEventHandlers, this.subscriptionManagerFactory.create(), this.lspTextCreationService);

                return Optional.of(lspTextEventProcessor)
                        .filter(representationEventProcessorClass::isInstance)
                        .map(representationEventProcessorClass::cast);
                // @formatter:on
            }
        }
        return Optional.empty();
    }
}
