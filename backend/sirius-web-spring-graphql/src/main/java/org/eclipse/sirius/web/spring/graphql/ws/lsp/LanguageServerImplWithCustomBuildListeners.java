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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.server.ILanguageServerAccess.IBuildListener;
import org.eclipse.xtext.ide.server.LanguageServerImpl;

/**
 * We extend the Xtext implementation of {@link LanguageServer} with a hook allowing us to add custom
 * {@link IBuildListener} implementations at initialization time.
 *
 * @author flatombe
 */
public class LanguageServerImplWithCustomBuildListeners extends LanguageServerImpl {

    private Collection<IBuildListener> customBuildListeners = new LinkedHashSet<>();

    public LanguageServerImplWithCustomBuildListeners() {
    }

    public void addCustomBuildListener(IBuildListener customBuildListener) {
        this.customBuildListeners.add(customBuildListener);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final CompletableFuture<InitializeResult> result = super.initialize(params);

        this.customBuildListeners.forEach(customBuildListener -> this.getLanguageServerAccess().addBuildListener(customBuildListener));

        return result;
    }
}
