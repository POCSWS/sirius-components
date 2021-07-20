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
import java.util.UUID;

import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.core.api.IRepresentationInput;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.representations.IRepresentation;
import org.eclipse.sirius.web.spring.collaborative.api.ChangeDescription;
import org.eclipse.sirius.web.spring.collaborative.api.ChangeKind;
import org.eclipse.sirius.web.spring.collaborative.api.ISubscriptionManager;
import org.eclipse.sirius.web.spring.collaborative.dto.RenameRepresentationInput;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextContext;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextCreationService;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextEventHandler;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextEventProcessor;
import org.eclipse.sirius.web.spring.collaborative.lsp.api.ILspTextInput;
import org.eclipse.sirius.web.spring.collaborative.lsp.dto.RenameLspTextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * {@link ILspTextEventProcessor} implementation that handles changes in an {@link LspText} and notifies subscribers of
 * these changes.
 *
 * @author flatombe
 */
public class LspTextEventProcessor implements ILspTextEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(LspTextEventProcessor.class);

    private final IEditingContext editingContext;

    private final ILspTextContext lspTextContext;

    private final List<ILspTextEventHandler> lspTextEventHandlers;

    private final ISubscriptionManager subscriptionManager;

    private final ILspTextCreationService lspTextCreationService;

    private final Many<Boolean> canBeDisposedSink = Sinks.many().unicast().onBackpressureBuffer();

    private final LspTextEventFlux lspTextEventFlux;

    public LspTextEventProcessor(IEditingContext editingContext, ILspTextContext lspTextContext, List<ILspTextEventHandler> lspTextEventHandlers, ISubscriptionManager subscriptionManager,
            ILspTextCreationService lspTextCreationService) {
        this.editingContext = Objects.requireNonNull(editingContext);
        this.lspTextContext = Objects.requireNonNull(lspTextContext);
        this.lspTextEventHandlers = Objects.requireNonNull(lspTextEventHandlers);
        this.subscriptionManager = Objects.requireNonNull(subscriptionManager);
        this.lspTextCreationService = Objects.requireNonNull(lspTextCreationService);

        // We automatically refresh the representation before using it since things may have changed since the moment it
        // has been saved in the database. This is quite similar to the auto-refresh on loading in Sirius.
        LspText lspText = this.lspTextCreationService.refresh(editingContext, lspTextContext).orElse(null);
        lspTextContext.update(lspText);
        this.lspTextEventFlux = new LspTextEventFlux(lspText);

        if (lspText != null) {
            this.logger.trace("LspText refreshed: {})", lspText.getId()); //$NON-NLS-1$
        }
    }

    @Override
    public IRepresentation getRepresentation() {
        return this.lspTextContext.getLspText();
    }

    @Override
    public ISubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IRepresentationInput representationInput) {
        IRepresentationInput effectiveInput = representationInput;
        if (representationInput instanceof RenameRepresentationInput) {
            RenameRepresentationInput renameRepresentationInput = (RenameRepresentationInput) representationInput;
            effectiveInput = new RenameLspTextInput(renameRepresentationInput.getId(), renameRepresentationInput.getEditingContextId(), renameRepresentationInput.getRepresentationId(),
                    renameRepresentationInput.getNewLabel());
        }
        if (effectiveInput instanceof ILspTextInput) {
            ILspTextInput lspTextInput = (ILspTextInput) effectiveInput;

            Optional<ILspTextEventHandler> optionalLspTextEventHandler = this.lspTextEventHandlers.stream().filter(handler -> handler.canHandle(lspTextInput)).findFirst();

            if (optionalLspTextEventHandler.isPresent()) {
                ILspTextEventHandler lspTextEventHandler = optionalLspTextEventHandler.get();
                lspTextEventHandler.handle(payloadSink, changeDescriptionSink, this.editingContext, this.lspTextContext, lspTextInput);
            } else {
                this.logger.warn("No handler found for event: {}", lspTextInput); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void refresh(ChangeDescription changeDescription) {
        if (this.shouldRefresh(changeDescription)) {
            LspText refreshedLspText = this.lspTextCreationService.refresh(this.editingContext, this.lspTextContext).orElse(null);
            if (refreshedLspText != null) {
                this.logger.trace("LspText refreshed: {}", refreshedLspText.getId()); //$NON-NLS-1$
            }
            this.lspTextContext.reset();
            this.lspTextContext.update(refreshedLspText);
            this.lspTextEventFlux.lspTextRefreshed(changeDescription.getInput(), refreshedLspText);
        }
    }

    /**
     * A lspText is refresh if there is a semantic change or if there is a lspText layout change coming from this very
     * lspText (not other lspTexts)
     *
     * @param changeDescription
     *            The change description
     * @return <code>true</code> if the lspText should be refreshed, <code>false</code> otherwise
     */
    private boolean shouldRefresh(ChangeDescription changeDescription) {
        return ChangeKind.SEMANTIC_CHANGE.equals(changeDescription.getKind());

    }

    @Override
    public Flux<IPayload> getOutputEvents(IInput input) {
        // @formatter:off
        return Flux.merge(
            this.lspTextEventFlux.getFlux(input),
            this.subscriptionManager.getFlux(input)
        )
        .doOnSubscribe(subscription -> {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            this.subscriptionManager.add(input, username);
            this.logger.trace("{} has subscribed to the lspText {} {}", username, this.lspTextContext.getLspText().getId(), this.subscriptionManager); //$NON-NLS-1$
        })
        .doOnCancel(() -> {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            this.subscriptionManager.remove(UUID.randomUUID(), username);
            this.logger.trace("{} has unsubscribed from the lspText {} {}", username, this.lspTextContext.getLspText().getId(), this.subscriptionManager); //$NON-NLS-1$

            if (this.subscriptionManager.isEmpty()) {
                EmitResult emitResult = this.canBeDisposedSink.tryEmitNext(Boolean.TRUE);
                if (emitResult.isFailure()) {
                    String pattern = "An error has occurred while emitting that the processor can be disposed: {}"; //$NON-NLS-1$
                    this.logger.warn(pattern, emitResult);
                }
            }
        });
        // @formatter:on
    }

    @Override
    public Flux<Boolean> canBeDisposed() {
        return this.canBeDisposedSink.asFlux();
    }

    @Override
    public void dispose() {
        this.logger.trace("Disposing the lspText event processor {}", this.lspTextContext.getLspText().getId()); //$NON-NLS-1$

        this.subscriptionManager.dispose();
        this.lspTextEventFlux.dispose();
    }

}
