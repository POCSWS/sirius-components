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
package org.eclipse.sirius.web.spring.graphql.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.sirius.web.collaborative.api.services.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.web.lsp.description.LspTextDescription;
import org.eclipse.sirius.web.services.api.representations.IRepresentationDescriptionService;
import org.eclipse.sirius.web.spring.graphql.api.URLConstants;
import org.eclipse.sirius.web.spring.graphql.ws.GraphQLWebSocketHandler;
import org.eclipse.sirius.web.spring.graphql.ws.lsp.LanguageServerWebSocketHandler;
import org.eclipse.sirius.web.spring.graphql.ws.lsp.TextualLanguageRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import graphql.GraphQL;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Spring configuration used to register all the Web Socket endpoints.
 * <p>
 * This class is used to creates the /subscriptions GraphQL endpoint to add support for GraphQL subscriptions.
 * </p>
 *
 * @author sbegaudeau
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final Logger logger = LoggerFactory.getLogger(WebSocketConfiguration.class);

    private final String allowedOrigins;

    private final GraphQL graphQL;

    private final ObjectMapper objectMapper;

    private final MeterRegistry meterRegistry;

    private final IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    private final TextualLanguageRegistry textualLanguageRegistry;

    private final IRepresentationDescriptionService representationDescriptionService;

    public WebSocketConfiguration(@Value("${sirius.web.graphql.websocket.allowed.origins}") String allowedOrigins, GraphQL graphQL, ObjectMapper objectMapper, MeterRegistry meterRegistry,
            IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry, TextualLanguageRegistry textualLanguagesRegistry,
            IRepresentationDescriptionService representationDescriptionService) {
        this.allowedOrigins = Objects.requireNonNull(allowedOrigins);
        this.graphQL = Objects.requireNonNull(graphQL);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
        this.editingContextEventProcessorRegistry = Objects.requireNonNull(editingContextEventProcessorRegistry);
        this.textualLanguageRegistry = Objects.requireNonNull(textualLanguagesRegistry);
        this.representationDescriptionService = Objects.requireNonNull(representationDescriptionService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        GraphQLWebSocketHandler graphQLWebSocketHandler = new GraphQLWebSocketHandler(this.objectMapper, this.graphQL, this.meterRegistry);
        WebSocketHandlerRegistration graphQLWebSocketRegistration = registry.addHandler(graphQLWebSocketHandler, URLConstants.GRAPHQL_SUBSCRIPTION_PATH);
        graphQLWebSocketRegistration.setAllowedOrigins(this.allowedOrigins);

        List<LspTextDescription> allRegisteredTextualRepresentationDescriptions = this.representationDescriptionService.getRepresentationDescriptions().stream()
                .filter(LspTextDescription.class::isInstance).map(LspTextDescription.class::cast).collect(Collectors.toList());
        // All textual DSLs share the same WebSocketHandler.
        LanguageServerWebSocketHandler languageServerWebSocketHandler = new LanguageServerWebSocketHandler(this.objectMapper, this.meterRegistry, this.editingContextEventProcessorRegistry,
                this.textualLanguageRegistry);
        allRegisteredTextualRepresentationDescriptions.forEach(lspTextDescription -> {
            // WebSocket URL is expected to look like
            // "xxx/language-servers/$languageName/$editingContextId/$representationId".
            WebSocketHandlerRegistration languageServerWebSocketRegistration = registry
                    .addHandler(languageServerWebSocketHandler, URLConstants.LANGUAGE_SERVERS_PATH + "/" + lspTextDescription.getLanguageName() + "/*/*") //$NON-NLS-1$ //$NON-NLS-2$
                    .addInterceptors(this.createLanguageServerWebSocketInterceptor());
            languageServerWebSocketRegistration.setAllowedOrigins(this.allowedOrigins);
        });
    }

    @Bean
    public HandshakeInterceptor createLanguageServerWebSocketInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                final String path = request.getURI().getPath();
                // URI of the WebSocketSession should look like "wss://languageName/editingContextId/representationId".
                if (StringUtils.countOccurrencesOf(path, "/") == 4) { //$NON-NLS-1$
                    final String lastSegment = path.substring(path.lastIndexOf('/') + 1);
                    final String pathWithoutLastSegment = path.substring(0, path.lastIndexOf('/'));
                    final String segmentBeforeLastSegment = pathWithoutLastSegment.substring(pathWithoutLastSegment.lastIndexOf('/') + 1);
                    final String pathWithoutLastTwoSegments = pathWithoutLastSegment.substring(0, pathWithoutLastSegment.lastIndexOf('/'));
                    final String segmentBeforeLastTwoSegments = pathWithoutLastTwoSegments.substring(pathWithoutLastTwoSegments.lastIndexOf('/') + 1);

                    final String languageName = segmentBeforeLastTwoSegments;
                    final UUID editingContextId = UUID.fromString(segmentBeforeLastSegment);
                    final UUID representationId = UUID.fromString(lastSegment);

                    attributes.put("languageName", languageName); //$NON-NLS-1$
                    attributes.put("editingContextId", editingContextId); //$NON-NLS-1$
                    attributes.put("representationId", representationId); //$NON-NLS-1$

                    return true;
                } else {
                    WebSocketConfiguration.this.logger.error("WebSocket URI path is not as expected: {}", path); //$NON-NLS-1$
                    return false;
                }
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                // Nothing to do after handshake
            }
        };
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(100000);
        container.setMaxBinaryMessageBufferSize(100000);
        return container;
    }

}
