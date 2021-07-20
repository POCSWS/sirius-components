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
import * as monaco from 'monaco-editor-core';
import { Disposable, MessageConnection, MonacoLanguageClient, MonacoServices } from 'monaco-languageclient';

export interface MonacoEditorRuntime {
  // DSL-specific
  readonly monacoDslConfiguration: MonacoDslConfiguration;
  // DSL-specific
  readonly monacoEditor: monaco.editor.IStandaloneCodeEditor;
  // DSL-and-model-specific because WebSocket URL contains DSL name, model editingContextId and representationId
  readonly webSocket: WebSocket;
  // model-and-connection-specific
  currentJsonRpcConnection: MessageConnection;
  // model-and-connection-specific
  currentMonacoLanguageClient: MonacoLanguageClient;
  // model-and-connection-specific
  currentMonacoLanguageClientRunning: Disposable;
}

export interface MonacoDslConfiguration {
  // DSL-specific... I think?
  readonly monacoServices: MonacoServices;
  // DSL-specific
  readonly dslName: string;
  // DSL-specific
  readonly dslThemeName: string;
  // DSL-specific
  readonly dslExtension: string;
}
