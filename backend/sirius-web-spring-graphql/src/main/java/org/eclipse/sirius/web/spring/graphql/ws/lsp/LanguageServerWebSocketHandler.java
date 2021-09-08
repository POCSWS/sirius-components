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
 * The entry point of the GraphQL Web Socket API.
 * <p>
 * This endpoint will be available on the /subscriptions path. Since the Web Socket API will not be used to retrieve
 * static resources, the path will not be prefixed by an API prefix. As such, users will be able to send GraphQL
 * queries, mutations and subscriptions to the following URL:
 * </p>
 *
 * <pre>
 * PROTOCOL://DOMAIN.TLD(:PORT)/subscriptions
 * </pre>
 *
 * <p>
 * In a development environment, the URL will most likely be:
 * </p>
 *
 * <pre>
 * ws://localhost:8080/subscriptions
 * </pre>
 *
 * <p>
 * During the initial handshake, clients have to indicate that they will use the "graphql-ws" Web Socket subprotocol.
 * See http://tools.ietf.org/html/rfc6455#section-1.9 for additional information on this subprotocol support.
 * </p>
 *
 * <p>
 * Once the connection has been established, users can send an initial request to ensure that the server is up and ready
 * to handle their GraphQL requests:
 * </p>
 *
 * <pre>
 * {
 *   "type": "connection_init"
 * }
 * </pre>
 *
 * <p>
 * The server will respond with an acknowledgment to let the user know that they can start sending requests.
 * </p>
 *
 * <pre>
 * {
 *   "type": "connection_ack"
 * }
 * </pre>
 *
 * <p>
 * GraphQL queries, mutations and subscriptions can be sent using a JSON object with the following content. The user is
 * responsible for the selection of the identifier. Thanks to this identifier, they will be able to figure out which
 * result matches a given request.
 * </p>
 *
 * <pre>
 * {
 *   "type": "start",
 *   "id": "ThisIsMyIdentifierUsedToRetrieveMyData"
 *   "payload": {
 *     "query": "...",
 *     "variables": {
 *       "key": "value"
 *     },
 *     "operationName": "..."
 *   }
 * }
 * </pre>
 *
 * <p>
 * In case of a subscription, at least one response will be returned to confirmed the subscription with the following
 * structure:
 * </p>
 *
 * <pre>
 * {
 *   "type": "data",
 *   "id": "..."
 * }
 * </pre>
 *
 * <p>
 * This response gives the users the identifier of their subscription. After that, the results of the execution of the
 * subscription will be returned using the following JSON data structure. The same structure will be used to return
 * results for a query or a mutation.
 * </p>
 *
 * <pre>
 * {
 *   "type": "data",
 *   "id": "...",
 *   "payload": {
 *     "data": { ... },
 *     "errors": [
 *       { ... }
 *     ]
 *   }
 * }
 * </pre>
 *
 * <p>
 * In order to unsubscribe, users should send a stop request using the identifier retrieved from the response.
 * </p>
 *
 * <pre>
 * {
 *   "type": "stop",
 *   "id": "..."
 * }
 * </pre>
 *
 * <p>
 * The server will confirm the unsubscription thanks an acknowledgment response too. On top of that, the server may send
 * a keep alive response from time to time to prevent the client from terminating the connection.
 * </p>
 *
 * @author sbegaudeau
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

    private final Map<WebSocketSession, LanguageServerRuntime> languageServerRuntimeByWebSocketSession = new HashMap<>();

    private final ObjectMapper objectMapper;

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

    // private final MeterRegistry meterRegistry;

    public LanguageServerWebSocketHandler(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        // this.meterRegistry = Objects.requireNonNull(meterRegistry);

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
        this.logger.info(String.format("[%s]WebSocket Connection Established", session.getId())); //$NON-NLS-1$
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // TODO: for now we maintain one LS per WebSocketSession.
        final LanguageServerRuntime languageServerRuntime = this.languageServerRuntimeByWebSocketSession.computeIfAbsent(session, LanguageServerRuntime::new);
        languageServerRuntime.forwardMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.logger.info("LSWSHandler.afterConnectionClosed" + session.toString()); //$NON-NLS-1$
        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication) {
            SecurityContextHolder.setContext(new SecurityContextImpl((Authentication) principal));
        }

        // Disposable keepAliveSubscription = this.sessions2keepAliveSubscriptions.remove(session);
        // keepAliveSubscription.dispose();

        this.languageServerRuntimeByWebSocketSession.get(session).shutdown();

        // Closing the connection will trigger the same behavior as indicating that the connection should be closed
        new ConnectionTerminateMessageHandler(session, new HashMap<>()).handle();
    }

}
