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
package org.eclipse.sirius.web.spring.collaborative.lsp.messages;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the collaborative form message service.
 *
 * @author sbegaudeau
 */
@Service
public class CollaborativeLspTextMessageService implements ICollaborativeLspTextMessageService {
    private final MessageSourceAccessor messageSourceAccessor;

    public CollaborativeLspTextMessageService(@Qualifier("collaborativeLspTextMessageSourceAccessor") MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = Objects.requireNonNull(messageSourceAccessor);
    }

    @Override
    public String invalidInput(String expectedInputTypeName, String receivedInputTypeName) {
        return this.messageSourceAccessor.getMessage("INVALID_INPUT", new Object[] { expectedInputTypeName, receivedInputTypeName }); //$NON-NLS-1$
    }

    @Override
    public String lspTextDescriptionNotFound(String id) {
        return this.messageSourceAccessor.getMessage("LSPTEXTDESCRIPTION_NOT_FOUND", new Object[] { id }); //$NON-NLS-1$
    }

    @Override
    public String targetObjectNotFound(String id) {
        return this.messageSourceAccessor.getMessage("TARGET_OBJECT_NOT_FOUND", new Object[] { id }); //$NON-NLS-1$
    }
}
