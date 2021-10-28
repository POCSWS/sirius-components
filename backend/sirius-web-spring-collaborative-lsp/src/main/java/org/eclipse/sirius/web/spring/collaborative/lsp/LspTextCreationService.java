/*******************************************************************************
 * Copyright (c) 2021 Obeo and others.
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.sirius.web.collaborative.api.services.IRepresentationPersistenceService;
import org.eclipse.sirius.web.collaborative.api.services.Monitoring;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextContext;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextCreationService;
import org.eclipse.sirius.web.components.Element;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.lsp.components.LspTextComponent;
import org.eclipse.sirius.web.lsp.components.LspTextComponentProps;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.eclipse.sirius.web.lsp.events.ILspTextEvent;
import org.eclipse.sirius.web.lsp.renderer.LspTextRenderer;
import org.eclipse.sirius.web.representations.VariableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * {@link Service} implementation of {@link ILspTextCreationService}.
 *
 * @author sbegaudeau
 */
@Service
public class LspTextCreationService implements ILspTextCreationService {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IObjectService objectService;

    private final Timer timer;

    private final Logger logger = LoggerFactory.getLogger(LspTextCreationService.class);

    public LspTextCreationService(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationPersistenceService representationPersistenceService,
            IObjectService objectService, MeterRegistry meterRegistry) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationPersistenceService = Objects.requireNonNull(representationPersistenceService);
        this.objectService = Objects.requireNonNull(objectService);

        // @formatter:off
        this.timer = Timer.builder(Monitoring.REPRESENTATION_EVENT_PROCESSOR_REFRESH)
                .tag(Monitoring.NAME, "lspText") //$NON-NLS-1$
                .register(meterRegistry);
        // @formatter:on
    }

    @Override
    public LspText create(String label, Object targetObject, LspTextDescription lspTextDescription, IEditingContext editingContext) {
        return this.doRender(label, targetObject, editingContext, lspTextDescription, Optional.empty());
    }

    @Override
    public Optional<LspText> refresh(IEditingContext editingContext, ILspTextContext lspTextContext) {
        LspText previousLspText = lspTextContext.getLspText();
        Optional<Object> maybeTargetObject = this.objectService.getObject(editingContext, previousLspText.getTargetObjectId());
        // @formatter:off
        Optional<LspTextDescription> maybeLspTextDescription = this.representationDescriptionSearchService.findById(previousLspText.getDescriptionId())
                .filter(LspTextDescription.class::isInstance)
                .map(LspTextDescription.class::cast);
        // @formatter:on

        if (maybeTargetObject.isPresent() && maybeLspTextDescription.isPresent()) {
            LspText lspText = this.doRender(previousLspText.getLabel(), maybeTargetObject.get(), editingContext, maybeLspTextDescription.get(), Optional.of(lspTextContext));
            return Optional.of(lspText);
        } else {
            // The target object and/or the representation description does not exist: we are unable to render a
            // representation.
            return Optional.empty();
        }
    }

    private LspText doRender(String label, Object targetObject, IEditingContext editingContext, LspTextDescription lspTextDescription, Optional<ILspTextContext> optionalLspTextContext) {
        long start = System.currentTimeMillis();

        VariableManager variableManager = new VariableManager();
        variableManager.put(LspTextDescription.LABEL, label);
        variableManager.put(VariableManager.SELF, targetObject);
        variableManager.put(IEditingContext.EDITING_CONTEXT, editingContext);

        // FLA: in diagrams this is used for the layout but I am not sure we will have similar cases for text.
        Optional<ILspTextEvent> optionalLspTextElementEvent = optionalLspTextContext.map(ILspTextContext::getLspTextEvent);
        Optional<LspText> optionalPreviousLspText = optionalLspTextContext.map(ILspTextContext::getLspText);

        // @formatter:off
        LspTextComponentProps props = LspTextComponentProps.newLspTextComponentProps()
                .variableManager(variableManager)
                .lspTextDescription(lspTextDescription)
                .previousLspText(optionalPreviousLspText)
                .build();
        // @formatter:on

        Element element = new Element(LspTextComponent.class, props);

        LspText newLspText = new LspTextRenderer(this.logger).render(element);

        this.representationPersistenceService.save(editingContext.getId(), newLspText);

        long end = System.currentTimeMillis();
        this.timer.record(end - start, TimeUnit.MILLISECONDS);

        return newLspText;
    }

}
