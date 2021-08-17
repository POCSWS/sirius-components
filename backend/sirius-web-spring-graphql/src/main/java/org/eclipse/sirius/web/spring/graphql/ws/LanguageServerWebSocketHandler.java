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
package org.eclipse.sirius.web.spring.graphql.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.sirius.web.spring.graphql.ws.handlers.ConnectionTerminateMessageHandler;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.ServerModule;
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

        this.logger.info("Started LSWSHandler"); //$NON-NLS-1$
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // CHECKSTYLE:OFF
        // FIXME: somehow our front end ends up creating several web socket connections.
        // For now we just want to be able to
        if (!this.languageServerRuntimeByWebSocketSession.containsKey(session)) {
            this.languageServerRuntimeByWebSocketSession.put(session, new LanguageServerRuntime(session));
        }
        final LanguageServerRuntime languageServerRuntime = this.languageServerRuntimeByWebSocketSession.get(session);

        final String messagePayload = message.getPayload();
        this.logger.info("[" + session.getId() + "]" + "LSWSHandler received: " + messagePayload); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Header part, cf.
        // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#headerPart
        languageServerRuntime.getWebSocketOut().write(("Content-Length: " + message.getPayloadLength() + "\r\n").getBytes(StandardCharsets.US_ASCII)); //$NON-NLS-1$ //$NON-NLS-2$
        languageServerRuntime.getWebSocketOut().write("\r\n".getBytes(StandardCharsets.US_ASCII)); //$NON-NLS-1$

        // Content part, cf.
        // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#contentPart
        languageServerRuntime.getWebSocketOut().write(messagePayload.getBytes(StandardCharsets.UTF_8));

        // This will trigger the language server to read the messages we just wrote.
        languageServerRuntime.getWebSocketOut().flush();
        // CHECKSTYLE:ON
    }

    /**
     *
     * @author flatombe
     */
    private class LanguageServerRuntime {
        private final WebSocketSession session;

        private final PipedInputStream languageServerIn = new PipedInputStream();

        private final OutputStream languageServerOut = new ByteArrayOutputStream() {
            // @Override
            // public synchronized void flush() throws IOException {
            // super.flush();
            // System.out.println("[" + LanguageServerRuntime.this.sessionId + "]" + "LSOut Flushing"); //$NON-NLS-1$
            // //$NON-NLS-2$ //$NON-NLS-3$
            // };
            @Override
            public synchronized void flush() throws IOException {
                super.flush();

                String responsePayload = new String(this.toByteArray(), StandardCharsets.UTF_8);
                // String responsePayload = CharStreams.toString(new
                // InputStreamReader(LanguageServerRuntime.this.webSocketIn, Charsets.UTF_8));
                int i = responsePayload.indexOf("\r\n\r\n"); //$NON-NLS-1$
                if (i > 0) {
                    responsePayload = responsePayload.substring(i + 4);
                }
                if (responsePayload.contains("\"result\":{\"isIncomplete\":false,\"items\":[]}")) { //$NON-NLS-1$
                    responsePayload = responsePayload.replace("\"result\":{\"isIncomplete\":false,\"items\":[]}", "\"result\":{\"isIncomplete\":false,\"items\":[\"AAA\"]}"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                System.out.println("[" + LanguageServerRuntime.this.session.getId() + "]" + "About to respond with: " + responsePayload); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                LanguageServerRuntime.this.session.sendMessage(new TextMessage(responsePayload));

                this.reset();
            }
        };

        private final PipedInputStream webSocketIn = new PipedInputStream();

        private final PipedOutputStream webSocketOut = new PipedOutputStream();

        LanguageServerRuntime(WebSocketSession session) {
            this.session = session;
            try {
                this.languageServerIn.connect(this.webSocketOut);
                // this.languageServerOut.connect(this.webSocketIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            LanguageServerWebSocketHandler.this.startStatemachineLanguageServer(this.session.getId(), this.languageServerIn, this.languageServerOut, this.webSocketIn);
        }

        public OutputStream getWebSocketOut() {
            return this.webSocketOut;
        }
    }

    // CHECKSTYLE:OFF
    private void startStatemachineLanguageServer(String sessionId, InputStream languageServerIn, OutputStream languageServerOut, InputStream webSocketIn) {
        // final LanguageServerImpl languageServer = Guice.createInjector(new
        // fr.obeo.dsl.designer.sample.StatemachineRuntimeModule()).getInstance(LanguageServerImpl.class);
        // languageServer.getTextDocumentService();
        // ServerLauncher launcher = Guice.createInjector(new ServerModule()).<ServerLauncher>
        // getInstance(ServerLauncher.class);

        final Injector xtextLanguageServerInjector = Guice.createInjector(new ServerModule());
        final LanguageServerImpl xtextLanguageServer = xtextLanguageServerInjector.getInstance(LanguageServerImpl.class);

        // final AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(new
        // InetSocketAddress("0.0.0.0", 5008)); //$NON-NLS-1$
        // final AsynchronousSocketChannel socketChannel = serverSocket.accept().get();

        // this.in = Channels.newInputStream(socketChannel);
        // this.out = Channels.newOutputStream(socketChannel);

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Function<MessageConsumer, MessageConsumer> wrapper = consumer -> consumer;

        final Launcher<LanguageClient> languageServerLauncher = new Launcher.Builder<LanguageClient>().setLocalService(xtextLanguageServer).setRemoteInterface(LanguageClient.class)
                .setInput(languageServerIn).setOutput(languageServerOut).setExecutorService(executorService).wrapMessages(wrapper).create();

        xtextLanguageServer.connect(languageServerLauncher.getRemoteProxy());
        System.out.println("[" + sessionId + "]" + "Starting LS..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        final Future<Future<?>> languageServerStarting = executorService.submit(() -> {
            final Future<?> languageServerListening = languageServerLauncher.startListening();
            System.out.println("[" + sessionId + "]" + "Did start LS"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            while (!languageServerListening.isDone()) {
                // ByteArrayOutputStream result = new ByteArrayOutputStream();
                // byte[] buffer = new byte[1024];
                // for (int length; (length = webSocketIn.read(buffer)) != -1;) {
                // result.write(buffer, 0, length);
                // }
                // final String responsePayload = result.toString(StandardCharsets.UTF_8.name());
                // String responsePayload = CharStreams.toString(new InputStreamReader(webSocketIn, Charsets.UTF_8));
                // this.logger.info("[" + sessionId + "]" + "Response: " + responsePayload); // $NON-NLS-1$
                // //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                // session.sendMessage(new TextMessage(responsePayload));
            }
            if (languageServerListening.isDone()) {
                System.out.println("[" + sessionId + "]" + "Shutting down LS..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                xtextLanguageServer.shutdown();
                System.out.println("[" + sessionId + "]" + "Did shut down LS"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return languageServerListening;
        });

        // final Future<?> future1 = future2.get();

        // LanguageServerImpl languageServer = Guice.createInjector(new ServerModule(), new StatemachineIdeModule(), new
        // StatemachineRuntimeModule()).getInstance(LanguageServerImpl.class);
        // ExecutorService threadPool = Executors.newCachedThreadPool();
        // Future<?> x = threadPool.submit(() -> {
        // Launcher<LanguageClient> launcher = Launcher.createLauncher(languageServer, LanguageClient.class, System.in,
        // System.out, true, new PrintWriter(System.out));
        // languageServer.connect(launcher.getRemoteProxy());
        // Future<Void> future = launcher.startListening();
        // System.out.println("Statemachine Xtext Language Server has been started."); //$NON-NLS-1$
        // try {
        // while (!future.isDone()) {
        // Thread.sleep(10_000L);
        // }
        // } catch (InterruptedException e) {
        // // throw Exceptions.sneakyThrow(e);
        // e.printStackTrace();
        // }
        // });
        // try {
        // Thread.sleep(10000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // languageServer.completion(new CompletionParams(new TextDocumentIdentifier("abcdef"), new Position(0, 0)))
        // //$NON-NLS-1$
        // .whenComplete((Either<List<CompletionItem>, CompletionList> result, Throwable exception) -> {
        // if (exception != null) {
        // System.out.println(exception);
        // } else {
        // if (result.getLeft() != null) {
        // System.out.println(result.getLeft());
        // } else {
        // System.out.println(result.getRight());
        // }
        // }
        // });
    }
    // CHECKSTYLE:ON

    // private void send(WebSocketSession session, IOperationMessage message) {
    // try {
    // String responsePayload = this.objectMapper.writeValueAsString(message);
    // TextMessage textMessage = new TextMessage(responsePayload);
    //
    // this.logger.info("Message sent: {}", message); //$NON-NLS-1$
    //
    // session.sendMessage(textMessage);
    // } catch (IOException exception) {
    // this.logger.error(exception.getMessage(), exception);
    // }
    // }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // @formatter:off
//        Disposable subscribe = Flux.interval(GRAPHQL_KEEP_ALIVE_INTERVAL)
//                .subscribe(data -> this.send(session, new ConnectionKeepAliveMessage()));
        // @formatter:on
        // this.sessions2keepAliveSubscriptions.put(session, subscribe);
        this.logger.info("LSWSHandler.afterConnectionEstablished" + session.toString()); //$NON-NLS-1$
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

        // Closing the connection will trigger the same behavior as indicating that the connection should be closed
        new ConnectionTerminateMessageHandler(session, new HashMap<>()).handle();
    }

}
