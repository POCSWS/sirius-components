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
package org.eclipse.sirius.web.spring.collaborative.lsp.handlers;

import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.web.collaborative.api.services.ChangeDescription;
import org.eclipse.sirius.web.collaborative.api.services.ChangeKind;
import org.eclipse.sirius.web.collaborative.api.services.EventHandlerResponse;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventHandler;
import org.eclipse.sirius.web.collaborative.api.services.Monitoring;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextContext;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextEventHandler;
import org.eclipse.sirius.web.collaborative.lsp.api.ILspTextInput;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceInput;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceSuccessPayload;
import org.eclipse.sirius.web.core.api.ErrorPayload;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.emf.services.EditingContext;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.services.api.representations.IRepresentationService;
import org.eclipse.sirius.web.spring.collaborative.messages.ICollaborativeMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link IEditingContextEventHandler} implementation for handling a {@link UpdateSemanticResourceInput}.
 *
 * @author flatombe
 */
@Service
public class UpdateSemanticResourceEventHandler implements ILspTextEventHandler {

    private final Logger logger = LoggerFactory.getLogger(UpdateSemanticResourceEventHandler.class);

    private final ICollaborativeMessageService messageService;

    private final IObjectService objectService;

    private final IRepresentationService representationService;

    private final Counter counter;

    public UpdateSemanticResourceEventHandler(ICollaborativeMessageService messageService, IObjectService objectService, IRepresentationService representationService, MeterRegistry meterRegistry) {
        this.messageService = Objects.requireNonNull(messageService);
        this.objectService = Objects.requireNonNull(objectService);
        this.representationService = Objects.requireNonNull(representationService);

        // @formatter:off
        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
        // @formatter:on
    }

    @Override
    public boolean canHandle(ILspTextInput input) {
        return input instanceof UpdateSemanticResourceInput;
    }

    @Override
    public EventHandlerResponse handle(IEditingContext editingContext, ILspTextContext lspTextContext, ILspTextInput input) {
        this.counter.increment();

        if (input instanceof UpdateSemanticResourceInput) {
            UpdateSemanticResourceInput updateSemanticResourceInput = (UpdateSemanticResourceInput) input;

            final boolean updateResultedInSemanticChanges = this.handleSemanticResourceUpdate(editingContext, updateSemanticResourceInput.getResource(), lspTextContext.getLspText());
            final String changeKind;
            if (updateResultedInSemanticChanges) {
                changeKind = ChangeKind.SEMANTIC_CHANGE;
            } else {
                changeKind = ChangeKind.NOTHING;
            }

            return new EventHandlerResponse(new ChangeDescription(changeKind, editingContext.getId()),
                    new UpdateSemanticResourceSuccessPayload(updateSemanticResourceInput.getId(), editingContext.getId().toString()));
        }
        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), UpdateSemanticResourceInput.class.getSimpleName());
        return new EventHandlerResponse(new ChangeDescription(ChangeKind.NOTHING, editingContext.getId()), new ErrorPayload(input.getId(), message));
    }

    /**
     * Performs the actual behavior resulting from receiving an {@link UpdateSemanticResourceInput}.
     *
     * @param editingContext
     *            the (non-{@code null}) {@link IEditingContext}.
     * @param parsedResource
     *            the (non-{@code null}) {@link Resource} as parsed by the Language Server.
     * @param lspText
     *            the (non-{@code null}) {@link LspText} to update.
     * @return {@code true} if any semantic changes were merged into the {@link LspText}. {@code false} otherwise.
     */
    private boolean handleSemanticResourceUpdate(IEditingContext editingContext, Resource parsedResource, LspText lspText) {
        // TODO: apparently we don't need the "representationId" attribute from WebSessionSocket.

        this.logger.info("LspText {} targets {}", lspText.getId(), lspText.getTargetObjectId()); //$NON-NLS-1$
        final EObject targetObject = (EObject) this.objectService.getObject(editingContext, lspText.getTargetObjectId())
                .orElseThrow(() -> new NoSuchElementException("Could not find object " + lspText.getTargetObjectId())); //$NON-NLS-1$
        final ResourceSet resourceSetToUpdate = ((EditingContext) editingContext).getDomain().getResourceSet();
        final Resource resourceToUpdate = resourceSetToUpdate.getResources().stream().filter(resource -> {
            // Here we want to find the Resource containing the Target Object of the LspText representation
            return targetObject.eResource().equals(resource);
        }).findFirst().orElseThrow();

        return this.performMerge(resourceToUpdate, parsedResource) > 0;
    }

    /**
     * Performs the merge by accepting all changes.
     *
     * @param resourceToUpdate
     *            the (non-{@code null}) {@link Resource} to merge into.
     * @param parsedResource
     *            the (non-{@code null}) {@link Resource} to merge.
     * @return the (positive) number of differences merged.
     */
    private int performMerge(Resource resourceToUpdate, Resource parsedResource) {
        final EMFCompare emfCompare = EMFCompare.builder().build();

        // By default, EMFCompare spills quite a few INFOs we do not care about in our context.
        doNotShowLog4jLoggerInfos();

        final IComparisonScope comparisonScope = new DefaultComparisonScope(resourceToUpdate, parsedResource, null);
        final Comparison comparison = emfCompare.compare(comparisonScope);

        // For now our policy is to merge all differences.
        List<Diff> differences = comparison.getDifferences();
        if (!differences.isEmpty()) {
            this.logger.info("Merging {} differences with EMF Compare.", differences.size()); //$NON-NLS-1$
            IMerger.Registry mergerRegistry = IMerger.RegistryImpl.createStandaloneInstance();
            IBatchMerger merger = new BatchMerger(mergerRegistry);
            merger.copyAllRightToLeft(differences, new BasicMonitor());
        }
        return differences.size();
    }

    private static void doNotShowLog4jLoggerInfos() {
        @SuppressWarnings("unchecked")
        final Enumeration<org.apache.log4j.Logger> allLoggers = LogManager.getCurrentLoggers();
        while (allLoggers.hasMoreElements()) {
            org.apache.log4j.Logger logger = allLoggers.nextElement();
            logger.setLevel(Level.WARN);
        }
    }

}
