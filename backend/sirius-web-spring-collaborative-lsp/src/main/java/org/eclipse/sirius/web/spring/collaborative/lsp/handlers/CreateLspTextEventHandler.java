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
package org.eclipse.sirius.web.spring.collaborative.lsp.handlers;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.web.collaborative.api.dto.CreateRepresentationInput;
import org.eclipse.sirius.web.collaborative.api.dto.CreateRepresentationSuccessPayload;
import org.eclipse.sirius.web.collaborative.api.services.ChangeDescription;
import org.eclipse.sirius.web.collaborative.api.services.ChangeKind;
import org.eclipse.sirius.web.collaborative.api.services.EventHandlerResponse;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventHandler;
import org.eclipse.sirius.web.collaborative.api.services.Monitoring;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextCreationService;
import org.eclipse.sirius.web.core.api.ErrorPayload;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.eclipse.sirius.web.spring.collaborative.lsp.messages.ICollaborativeLspTextMessageService;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link IEditingContextEventHandler} implementation for creating {@link LspText} representations.
 *
 * @author flatombe
 */
@Service
public class CreateLspTextEventHandler implements IEditingContextEventHandler {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final ILspTextCreationService lspTextCreationService;

    private final IObjectService objectService;

    private final ICollaborativeLspTextMessageService messageService;

    private final Counter counter;

    public CreateLspTextEventHandler(IRepresentationDescriptionSearchService representationDescriptionSearchService, ILspTextCreationService lspTextCreationService, IObjectService objectService,
            ICollaborativeLspTextMessageService messageService, MeterRegistry meterRegistry) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.lspTextCreationService = Objects.requireNonNull(lspTextCreationService);
        this.objectService = Objects.requireNonNull(objectService);
        this.messageService = Objects.requireNonNull(messageService);

        // @formatter:off
        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
        // @formatter:on
    }

    @Override
    public boolean canHandle(IInput input) {
        if (input instanceof CreateRepresentationInput) {
            CreateRepresentationInput createRepresentationInput = (CreateRepresentationInput) input;
            // @formatter:off
            return this.representationDescriptionSearchService.findById(createRepresentationInput.getRepresentationDescriptionId())
                    .filter(LspTextDescription.class::isInstance)
                    .isPresent();
            // @formatter:on
        }
        return false;
    }

    @Override
    public EventHandlerResponse handle(IEditingContext editingContext, IInput input) {
        this.counter.increment();

        String errorMessage = null;
        if (input instanceof CreateRepresentationInput) {
            CreateRepresentationInput createRepresentationInput = (CreateRepresentationInput) input;

            final UUID representationDescriptionId = createRepresentationInput.getRepresentationDescriptionId();

            // @formatter:off
            Optional<LspTextDescription> optionalLspTextDescription = this.representationDescriptionSearchService.findById(representationDescriptionId)
                    .filter(LspTextDescription.class::isInstance)
                    .map(LspTextDescription.class::cast);
            // @formatter:on

            if (optionalLspTextDescription.isPresent()) {
                final String targetObjectId = createRepresentationInput.getObjectId();
                Optional<Object> optionalObject = this.objectService.getObject(editingContext, targetObjectId);
                if (optionalObject.isPresent()) {
                    LspText createdLspText = this.lspTextCreationService.create(createRepresentationInput.getRepresentationName(), optionalObject.get(), optionalLspTextDescription.get(),
                            editingContext);
                    return new EventHandlerResponse(new ChangeDescription(ChangeKind.REPRESENTATION_CREATION, editingContext.getId()),
                            new CreateRepresentationSuccessPayload(input.getId(), createdLspText));
                } else {
                    errorMessage = this.messageService.targetObjectNotFound(targetObjectId);
                }
            } else {
                errorMessage = this.messageService.lspTextDescriptionNotFound(representationDescriptionId.toString());
            }
        }

        if (errorMessage == null) {
            errorMessage = this.messageService.invalidInput(input.getClass().getSimpleName(), CreateRepresentationInput.class.getSimpleName());
        }
        return new EventHandlerResponse(new ChangeDescription(ChangeKind.NOTHING, editingContext.getId()), new ErrorPayload(input.getId(), errorMessage));
    }

}
