/*******************************************************************************
 * Copyright (c) 2021 Obeo and others.
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

export interface LspTextWebSocketContainerProps {
  editingContextId: string;
}

export interface GQLLspTextEventSubscription {
  lspTextEvent: GQLLspTextEventPayload;
}

export interface GQLLspTextEventPayload {
  __typename: string;
}

export interface GQLLspTextRefreshedEventPayload extends GQLLspTextEventPayload {
  id: string;
  lspText: LspText;
}

export interface LspText {
  id: string;
  label: string;
  contents: string;
}
