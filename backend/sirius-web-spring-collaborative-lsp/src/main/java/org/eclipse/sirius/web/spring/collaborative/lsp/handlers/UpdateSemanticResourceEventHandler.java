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

import com.google.common.base.Function;

import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
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
import org.omg.sysml.lang.sysml.Relationship;
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

        this.logger.trace("LspText {} targets {}", lspText.getId(), lspText.getTargetObjectId()); //$NON-NLS-1$
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
        /**
         * Horrible hack to make SysML v2 metamodel work through EMFCompare.
         */
        Function<EObject, String> idFunction = new Function<>() {
            // CHECKSTYLE:OFF
            @Override
            public String apply(EObject input) {
                if (input instanceof org.omg.sysml.lang.sysml.Element) {
                    final org.omg.sysml.lang.sysml.Element element = (org.omg.sysml.lang.sysml.Element) input;
                    if (element.getName() != null) {
                        return element.getName();
                    } else if (!element.getOwnedRelationship().isEmpty()) {
                        return element.eClass().getName() + "/" //$NON-NLS-1$
                                + element.getOwnedRelationship().stream().map(Relationship::getOwnedRelatedElement).flatMap(List::stream).map(this::apply).collect(Collectors.joining("::")); //$NON-NLS-1$
                    } else if (element instanceof org.omg.sysml.lang.sysml.Relationship) {
                        final org.omg.sysml.lang.sysml.Relationship relationship = (org.omg.sysml.lang.sysml.Relationship) element;
                        return element.eClass().getName() + "/" + relationship.getOwnedRelatedElement().stream().map(this::apply).collect(Collectors.joining("::")); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        throw new IllegalArgumentException("No reliable ID found for: " + element.toString()); //$NON-NLS-1$
                    }
                }
                // a null return here tells the match engine to fall back to the other matchers
                return null;
            }
            // CHECKSTYLE:ON
        };
        // Using this matcher as fall back, EMF Compare will still search for XMI IDs on EObjects
        // for which we had no custom id function.
        IEObjectMatcher fallBackMatcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.WHEN_AVAILABLE);
        IEObjectMatcher customIDMatcher = new IdentifierEObjectMatcher(fallBackMatcher, idFunction);
        new IdentifierEObjectMatcher(fallBackMatcher, idFunction);
        IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
        IMatchEngine.Factory.Registry customRegistry = MatchEngineFactoryRegistryImpl.createStandaloneInstance();
        final MatchEngineFactoryImpl matchEngineFactory = new MatchEngineFactoryImpl(customIDMatcher, comparisonFactory);
        matchEngineFactory.setRanking(20); // default engine ranking is 10, must be higher to override.
        customRegistry.add(matchEngineFactory);
        ////

        final EMFCompare emfCompare = EMFCompare.builder().setMatchEngineFactoryRegistry(customRegistry).build();

        // By default, EMFCompare spills quite a few INFOs we do not care about in our context.
        doNotShowEmfCompareLog4jLoggerInfos();

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

    private static void doNotShowEmfCompareLog4jLoggerInfos() {
        @SuppressWarnings("unchecked")
        final Enumeration<org.apache.log4j.Logger> allLoggers = LogManager.getCurrentLoggers();
        while (allLoggers.hasMoreElements()) {
            org.apache.log4j.Logger logger = allLoggers.nextElement();
            if (logger.getName().startsWith("org.eclipse.emf.compare")) { //$NON-NLS-1$
                logger.setLevel(Level.WARN);
            }
        }
    }

}
