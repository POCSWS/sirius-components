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

/**
 * Interface of the collaborative form message service.
 *
 * @author sbegaudeau
 */
public interface ICollaborativeLspTextMessageService {
    String invalidInput(String expectedInputTypeName, String receivedInputTypeName);

    String lspTextDescriptionNotFound(String id);

    String targetObjectNotFound(String id);
}
