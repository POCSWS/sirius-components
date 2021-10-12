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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.lsp.LspText;
import org.eclipse.sirius.web.spring.graphql.ws.handlers.ConnectionTerminateMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link TextWebSocketHandler} implementation for handling web socket sessions used for {@link LspText}
 * representations.
 *
 * @author flatombe
 */
public class LanguageServerWebSocketHandler extends TextWebSocketHandler {

    // private static final Duration GRAPHQL_KEEP_ALIVE_INTERVAL = Duration.ofSeconds(28);
    //
    // private static final String COUNTER_METRIC_NAME = "siriusweb_graphql_ws_messages"; //$NON-NLS-1$
    //
    // private static final String TIMER_METRIC_NAME = "siriusweb_graphql_ws_sessions"; //$NON-NLS-1$
    //
    // private static final String MESSAGE = "message"; //$NON-NLS-1$
    //
    // private static final String GRAPHQL_WS = "graphql-ws"; //$NON-NLS-1$
    //
    // private static final String TYPE = "type"; //$NON-NLS-1$

    private final Logger logger = LoggerFactory.getLogger(LanguageServerWebSocketHandler.class);

    private final Map<WebSocketSession, LanguageServerRuntime> languageServerRuntimeByWebSocketSession = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    private final TextualLanguageRegistry textualLanguageRegistry;

    // private final Map<WebSocketSession, List<SubscriptionEntry>> sessions2entries = new ConcurrentHashMap<>();

    // private final Map<WebSocketSession, Disposable> sessions2keepAliveSubscriptions = new ConcurrentHashMap<>();

    // private final Counter connectionInitCounter;
    //
    // private final Counter startMessageCounter;
    //
    // private final Counter stopMessageCounter;
    //
    // private final Counter connectionTerminateCounter;
    //
    // private final Counter connectionErrorCounter;

    private final MeterRegistry meterRegistry;

    public LanguageServerWebSocketHandler(ObjectMapper objectMapper, MeterRegistry meterRegistry, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry,
            TextualLanguageRegistry textualLanguagesRegistry) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
        this.editingContextEventProcessorRegistry = Objects.requireNonNull(editingContextEventProcessorRegistry);
        this.textualLanguageRegistry = Objects.requireNonNull(textualLanguagesRegistry);

//         @formatter:off
//        this.startMessageCounter = Counter.builder(COUNTER_METRIC_NAME)
//                .tag(MESSAGE, "Start") //$NON-NLS-1$
//                .register(meterRegistry);
//        this.stopMessageCounter = Counter.builder(COUNTER_METRIC_NAME)
//                .tag(MESSAGE, "Stop") //$NON-NLS-1$
//                .register(meterRegistry);
//        this.connectionInitCounter = Counter.builder(COUNTER_METRIC_NAME)
//                .tag(MESSAGE, "Connection Init") //$NON-NLS-1$
//                .register(meterRegistry);
//        this.connectionErrorCounter = Counter.builder(COUNTER_METRIC_NAME)
//                .tag(MESSAGE, "Connection Error") //$NON-NLS-1$
//                .register(meterRegistry);
//        this.connectionTerminateCounter = Counter.builder(COUNTER_METRIC_NAME)
//                .tag(MESSAGE, "Connection Terminate") //$NON-NLS-1$
//                .register(meterRegistry);
//        Gauge.builder(TIMER_METRIC_NAME, this.sessions2keepAliveSubscriptions.keySet()::size)
//                .register(meterRegistry);
//         @formatter:on
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // @formatter:off
//        Disposable subscribe = Flux.interval(GRAPHQL_KEEP_ALIVE_INTERVAL)
//                .subscribe(data -> this.send(session, new ConnectionKeepAliveMessage()));
        // @formatter:on
        // this.sessions2keepAliveSubscriptions.put(session, subscribe);
        this.logger.info("[{}]Establishing WebSocketSession connection at {}", session.getId(), session.getUri()); //$NON-NLS-1$
        if (this.languageServerRuntimeByWebSocketSession.containsKey(session)) {
            throw new IllegalStateException("There already is a Language Server runtime for WebSocketSession " + session.getId()); //$NON-NLS-1$
        } else {
            LanguageServerRuntime.tryToCreate(session, this.editingContextEventProcessorRegistry, this.textualLanguageRegistry).ifPresentOrElse(languageServerRuntime -> {
                this.languageServerRuntimeByWebSocketSession.put(session, languageServerRuntime);
                this.logger.info("[{}]...established WebSocket Connection ({} active connections)", session.getId(), this.languageServerRuntimeByWebSocketSession.size()); //$NON-NLS-1$
            }, () -> {
                this.logger.warn("[{}]Could not create {}" + LanguageServerRuntime.class.getSimpleName()); //$NON-NLS-1$
            });
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication) {
            SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
        }

        this.languageServerRuntimeByWebSocketSession.get(session).forwardMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.logger.info("[{}]Closing WebSocketSession connection...", session.getId()); //$NON-NLS-1$

        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication) {
            SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
        }

        // Disposable keepAliveSubscription = this.sessions2keepAliveSubscriptions.remove(session);
        // keepAliveSubscription.dispose();

        this.languageServerRuntimeByWebSocketSession.get(session).shutdown();
        this.languageServerRuntimeByWebSocketSession.remove(session);

        // Closing the connection will trigger the same behavior as indicating that the connection should be closed
        new ConnectionTerminateMessageHandler(session, new HashMap<>()).handle();

        this.logger.info("[{}]...closed WebSocketSession connection ({} remaining connections)", session.getId(), this.languageServerRuntimeByWebSocketSession.size()); //$NON-NLS-1$
    }

}
