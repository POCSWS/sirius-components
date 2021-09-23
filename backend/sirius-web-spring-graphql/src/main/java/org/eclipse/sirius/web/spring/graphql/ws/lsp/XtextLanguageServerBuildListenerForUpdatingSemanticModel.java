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
package org.eclipse.sirius.web.spring.graphql.ws.lsp;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.xtext.ide.server.ILanguageServerAccess.IBuildListener;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.socket.WebSocketSession;

/**
 * {@link IBuildListener} implementation whose purpose is to trigger an update of the semantic model in the backend
 * according to the newly-parsed version of the model that was modified through an {@link LspText} representation.
 *
 * @author flatombe
 */
@Deprecated
public class XtextLanguageServerBuildListenerForUpdatingSemanticModel implements IBuildListener {
    private final Logger logger = LoggerFactory.getLogger(XtextLanguageServerBuildListenerForUpdatingSemanticModel.class);

    private final IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    private final WebSocketSession session;

    public XtextLanguageServerBuildListenerForUpdatingSemanticModel(WebSocketSession session, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry) {
        this.editingContextEventProcessorRegistry = Objects.requireNonNull(editingContextEventProcessorRegistry);
        this.session = session;
    }

    @Override
    public void afterBuild(List<Delta> deltas) {
        this.logger.info("Received {} deltas after build", deltas.size()); //$NON-NLS-1$
        deltas.forEach(this::handleDelta);
    }

    private void handleDelta(Delta delta) {
        this.logger.info("Handling delta {}", delta.toString()); //$NON-NLS-1$
        Principal principal = this.session.getPrincipal();
        if (principal instanceof Authentication) {
            SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
        }
        if (delta.haveEObjectDescriptionsChanged()) {
            IResourceDescription newResourceDescription = delta.getNew();
            URI uri = newResourceDescription.getURI();
            this.logger.info("[{}]Resource has changed", uri); //$NON-NLS-1$

            // FIXME: these depend on the URIs of the documents opened in Monaco-editor so adjust accordingly.
            final String editingContextId = uri.segment(0);
            final String representationId = uri.segment(1);

            // TODO: maybe we need to add the representationId into our UpdateModelInput? So we can retrieve which EMF
            // Resource to impact?
            // this.editingContextEventProcessorRegistry.dispatchEvent(UUID.fromString(editingContextId), new
            // UpdateModelInput(delta));
        } else {
            this.logger.info("Delta had no changes."); //$NON-NLS-1$
        }
    }

}
