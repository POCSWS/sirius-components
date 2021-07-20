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

import java.util.Objects;

import org.eclipse.xtext.ISetup;

/**
 * The technical artefacts used to create and edit textual representations of a language.
 *
 * @author flatombe
 */
public class TextualLanguageArtefacts {

    private final Class<? extends ISetup> setupType;

    public TextualLanguageArtefacts(Class<? extends ISetup> setupType) {
        this.setupType = Objects.requireNonNull(setupType);
    }

    public Class<? extends ISetup> getSetupType() {
        return this.setupType;
    }
}
