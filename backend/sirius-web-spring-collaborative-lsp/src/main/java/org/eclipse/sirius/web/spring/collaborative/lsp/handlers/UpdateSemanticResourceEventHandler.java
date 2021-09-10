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

import java.util.Objects;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.web.collaborative.api.services.ChangeDescription;
import org.eclipse.sirius.web.collaborative.api.services.ChangeKind;
import org.eclipse.sirius.web.collaborative.api.services.EventHandlerResponse;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventHandler;
import org.eclipse.sirius.web.collaborative.api.services.Monitoring;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceInput;
import org.eclipse.sirius.web.collaborative.lsp.api.dto.UpdateSemanticResourceSuccessPayload;
import org.eclipse.sirius.web.core.api.ErrorPayload;
import org.eclipse.sirius.web.core.api.IEditService;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.emf.services.EditingContext;
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
public class UpdateSemanticResourceEventHandler implements IEditingContextEventHandler {

    private final Logger logger = LoggerFactory.getLogger(UpdateSemanticResourceEventHandler.class);

    private final ICollaborativeMessageService messageService;

    private final IObjectService objectService;

    private final IEditService editService;

    private final Counter counter;

    public UpdateSemanticResourceEventHandler(ICollaborativeMessageService messageService, IObjectService objectService, IEditService editService, MeterRegistry meterRegistry) {
        this.messageService = Objects.requireNonNull(messageService);
        this.objectService = Objects.requireNonNull(objectService);
        this.editService = Objects.requireNonNull(editService);

        // @formatter:off
        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
        // @formatter:on
    }

    @Override
    public boolean canHandle(IInput input) {
        return input instanceof UpdateSemanticResourceInput;
    }

    @Override
    public EventHandlerResponse handle(IEditingContext editingContext, IInput input) {
        this.counter.increment();

        if (input instanceof UpdateSemanticResourceInput) {
            UpdateSemanticResourceInput updateSemanticResourceInput = (UpdateSemanticResourceInput) input;

            // This Resource comes from the parsing of the LspText contents by the Xtext parser.
            Resource parsedResource = updateSemanticResourceInput.getResource();

            this.performMerge(editingContext, parsedResource);

            return new EventHandlerResponse(new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId()),
                    new UpdateSemanticResourceSuccessPayload(updateSemanticResourceInput.getId(), editingContext.getId().toString(), parsedResource.getURI().lastSegment()));
        }
        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), UpdateSemanticResourceInput.class.getSimpleName());
        return new EventHandlerResponse(new ChangeDescription(ChangeKind.NOTHING, editingContext.getId()), new ErrorPayload(input.getId(), message));
    }

    /**
     * Merges a {@link Resource} into an {@link IEditingContext}.
     *
     * @param editingContext
     *            the (non-{@code null}) {@link IEditingContext}.
     * @param parsedResource
     *            the (non-{@code null}) {@link Resource} to merge.
     */
    private void performMerge(IEditingContext editingContext, Resource parsedResource) {
        // Dependency to EMF. What would happen in non-EMF cases?
        ResourceSet resourceSetToUpdate = ((EditingContext) editingContext).getDomain().getResourceSet();

        // devtime: there is only one for now.
        final Resource resourceToUpdate = resourceSetToUpdate.getResources().get(0);
        // TODO: how to find the right Resource? Maybe we can retrieve it based on the representationId that could be
        // passed to us through UpdateModelInput?
        // Or we need more infos from the frontend that would endup in the URI / WebSocketSession parameters.

        this.logger.info("Merging {} into {}", parsedResource, resourceToUpdate); //$NON-NLS-1$

        TreeIterator<EObject> iterator = resourceToUpdate.getAllContents();
        while (iterator.hasNext()) {
            final EObject eObjectToUpdate = iterator.next();
            final String uriFragment = resourceToUpdate.getURIFragment(eObjectToUpdate);
            final EObject newEObject = parsedResource.getEObject(uriFragment);
            for (EAttribute eAttribute : eObjectToUpdate.eClass().getEAllAttributes()) {
                eObjectToUpdate.eSet(eAttribute, newEObject.eGet(eAttribute));
            }
        }

        // final URI deltaResourceUri = resource.getUri().trimFragment();
        // resourceSetToUpdate.getURIConverter().getURIMap().put(deltaResourceUri, resourceToUpdate.getURI());
        // EcoreUtil.replace(eObjectToUpdate, newEObject);
        //
        // resourceSetToUpdate.getURIConverter().getURIMap().remove(deltaResourceUri, resourceToUpdate.getURI());
        // }
    }

}
