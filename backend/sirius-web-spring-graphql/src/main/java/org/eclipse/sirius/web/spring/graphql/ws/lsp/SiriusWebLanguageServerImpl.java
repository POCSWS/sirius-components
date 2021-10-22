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

import com.google.inject.Singleton;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensDelta;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.server.ILanguageServerAccess.IBuildListener;
import org.eclipse.xtext.ide.server.LanguageServerImpl;

/**
 * We extend the Xtext implementation of {@link LanguageServer} with a hook allowing us to add custom
 * {@link IBuildListener} implementations at initialization time.
 *
 * @author flatombe
 */
@Singleton
public class SiriusWebLanguageServerImpl extends LanguageServerImpl {

    public SiriusWebLanguageServerImpl() {
    }

    @Override
    protected ServerCapabilities createServerCapabilities(InitializeParams params) {
        final ServerCapabilities serverCapabilities = super.createServerCapabilities(params);
        // serverCapabilities.setSemanticTokensProvider(new SemanticTokensWithRegistrationOptions());
        return serverCapabilities;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return super.completion(params);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return super.semanticTokensFull(params);
    }

    @Override
    public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(SemanticTokensDeltaParams params) {
        return super.semanticTokensFullDelta(params);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
        return super.semanticTokensRange(params);
    }

}
