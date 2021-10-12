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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.web.dsl.statemachine.xtext.StatemachineSiriusWebIdeSetup;
import org.eclipse.sirius.web.dsl.sysmlv2.xtext.SysMLSiriusWebIdeSetup;
import org.springframework.stereotype.Component;

/**
 * A registry where we index textual languages by their name.
 *
 * @author flatombe
 */
@Component
public class TextualLanguageRegistry {

    private final Map<String, TextualLanguageArtefacts> index = new LinkedHashMap<>();

    public TextualLanguageRegistry() {
        this.registerArtefacts("statemachine", new TextualLanguageArtefacts(StatemachineSiriusWebIdeSetup.class)); //$NON-NLS-1$
        this.registerArtefacts("sysml", new TextualLanguageArtefacts(SysMLSiriusWebIdeSetup.class)); //$NON-NLS-1$
    }

    /**
     * Provides the {@link TextualLanguageArtefacts} of a language.
     *
     * @param languageName
     *            the (non-{@code null}) name of the language.
     * @return the (non-{@code null}) {@link Optional} representing the {@link TextualLanguageArtefacts}.
     */
    public Optional<TextualLanguageArtefacts> getArtefacts(String languageName) {
        Objects.requireNonNull(languageName);

        return Optional.ofNullable(this.index.get(languageName));
    }

    /**
     * Registers a {@link TextualLanguageArtefacts} for a language.
     *
     * @param languageName
     *            the (non-{@code null}) name of the language.
     * @param artefacts
     *            the (non-{@code null}) {@link TextualLanguageArtefacts} of the language.
     */
    public void registerArtefacts(String languageName, TextualLanguageArtefacts artefacts) {
        Objects.requireNonNull(languageName);
        Objects.requireNonNull(artefacts);

        this.index.put(languageName, artefacts);
    }

}
