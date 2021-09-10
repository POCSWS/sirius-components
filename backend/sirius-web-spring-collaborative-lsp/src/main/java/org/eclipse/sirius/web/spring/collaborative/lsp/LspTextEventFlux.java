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
package org.eclipse.sirius.web.spring.collaborative.lsp;

import java.util.Objects;

import org.eclipse.sirius.web.collaborative.lsp.api.dto.LspTextRefreshedEventPayload;
import org.eclipse.sirius.web.core.api.IInput;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.lsp.LspText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

/**
 * Service used to manage the lspText event flux.
 *
 * @author sbegaudeau
 */
public class LspTextEventFlux {

    private final Logger logger = LoggerFactory.getLogger(LspTextEventFlux.class);

    private final Many<IPayload> sink = Sinks.many().multicast().directBestEffort();

    private LspText currentLspText;

    public LspTextEventFlux(LspText currentLspText) {
        this.currentLspText = Objects.requireNonNull(currentLspText);
    }

    public void lspTextRefreshed(IInput input, LspText newLspText) {
        this.logger.info("LspTextEventFlux.tryEmitNextRefreshedEventPayload"); //$NON-NLS-1$
        this.currentLspText = newLspText;
        EmitResult emitResult = this.sink.tryEmitNext(new LspTextRefreshedEventPayload(input.getId(), this.currentLspText));
        if (emitResult.isFailure()) {
            String pattern = "An error has occurred while emitting a LspTextRefreshedEventPayload: {}"; //$NON-NLS-1$
            this.logger.warn(pattern, emitResult);
        }
    }

    public Flux<IPayload> getFlux(IInput input) {
        var initialRefresh = Mono.fromCallable(() -> new LspTextRefreshedEventPayload(input.getId(), this.currentLspText));
        return Flux.concat(initialRefresh, this.sink.asFlux());
    }

    public void dispose() {
        this.logger.info("LspTextEventFlux.tryEmitComplete"); //$NON-NLS-1$
        EmitResult emitResult = this.sink.tryEmitComplete();
        if (emitResult.isFailure()) {
            String pattern = "An error has occurred while marking the publisher as complete: {}"; //$NON-NLS-1$
            this.logger.warn(pattern, emitResult);
        }
    }

}
