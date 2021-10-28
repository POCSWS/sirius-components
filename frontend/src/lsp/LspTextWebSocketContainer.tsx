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
import { useSubscription } from '@apollo/client';
import { listen } from '@codingame/monaco-jsonrpc';
import IconButton from '@material-ui/core/IconButton';
import Snackbar from '@material-ui/core/Snackbar';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import { useMachine } from '@xstate/react';
import { ServerContext } from 'common/ServerContext';
import gql from 'graphql-tag';
import * as monaco from 'monaco-editor-core';
import {
  CloseAction,
  createConnection,
  ErrorAction,
  MessageConnection,
  MonacoLanguageClient,
  MonacoServices,
} from 'monaco-languageclient';
import normalizeUrl from 'normalize-url';
import React, { useContext, useEffect, useRef } from 'react';
import { RepresentationComponentProps } from 'workbench/Workbench.types';
import { lspTextRefreshedEventPayloadFragment, subscribersUpdatedEventPayloadFragment } from './LspTextEventFragments';
// import { MessageConnection } from 'vscode-languageserver-protocol/node_modules/vscode-jsonrpc';
import { GQLLspTextEventSubscription, LspText } from './LspTextWebSocketContainer.types';
import {
  HideToastEvent,
  LspTextWebSocketContainerContext,
  LspTextWebSocketContainerEvent,
  lspTextWebSocketContainerMachine,
  SchemaValue,
  ShowToastEvent,
  SubscriptionCompletedEvent,
  SubscriptionEmittedResultEvent,
} from './LspTextWebSocketContainerMachine';
import { MonacoDslConfiguration, MonacoEditorRuntime } from './MonacoRuntime';

function createMonacoModelUri(
  monacoDslConfiguration: MonacoDslConfiguration,
  editingContextId: String,
  lspText: LspText
): monaco.Uri {
  // TODO: can we really use LspText's ID as URI? Do we need the backend to provide us with the actual JsonResource URI?
  // Important: the URI must be a valid EMF URI with the file extension of the language.
  return monaco.Uri.parse(
    'inmemory://dev/' + editingContextId + '/' + lspText.id + monacoDslConfiguration.dslExtension
  );
}

function createMonacoEditorRuntime(
  monacoDslConfiguration: MonacoDslConfiguration,
  container: HTMLElement,
  editingContextId: String,
  lspText: LspText,
  wsOrigin: String
): MonacoEditorRuntime {
  // Step 2: create the Monaco editor.
  const monacoStandaloneEditorCreationOptions = {
    theme: monacoDslConfiguration.dslThemeName,
    language: monacoDslConfiguration.dslName,
    glyphMargin: true,
    lightbulb: {
      enabled: true,
    },
  };
  const monacoStandaloneCodeEditor = monaco.editor.create(container, monacoStandaloneEditorCreationOptions);

  // Step 3: create the WebSocket we will use to communicate with the Language Server.
  // TODO: can we change at runtime the WS that is used? probably
  const webSocketUrlToLanguageServer = normalizeUrl(
    `${wsOrigin}/language-servers/${monacoDslConfiguration.dslName}/${editingContextId}/${lspText.id}`
  );
  console.log('LspTextWSC-webSocketUrlToLanguageServer' + wsOrigin);
  const languageServerWebSocket = new WebSocket(webSocketUrlToLanguageServer);

  // We create our result early because the connection and language client will only be known when the JSON-RPC connection gets established.
  const monacoEditorRuntime = {
    monacoDslConfiguration: monacoDslConfiguration,
    monacoEditor: monacoStandaloneCodeEditor,
    webSocket: languageServerWebSocket,
    currentJsonRpcConnection: null,
    currentMonacoLanguageClient: null,
    currentMonacoLanguageClientRunning: null,
  };

  listen({
    webSocket: languageServerWebSocket,
    logger: {
      error: (message: string) => console.error(message),
      warn: (message: string) => console.warn(message),
      info: (message: string) => console.info(message),
      log: (message: string) => console.log(message),
    },
    onConnection: (connection) => {
      const monacoModelUri = monacoStandaloneCodeEditor.getModel().uri;
      console.log(`[${monacoModelUri}]JSON-RPC Connection initialized`);
      monacoEditorRuntime.currentJsonRpcConnection = connection;
      const languageClient: MonacoLanguageClient = createMonacoLanguageClient(lspText, connection);
      monacoEditorRuntime.currentMonacoLanguageClient = languageClient;

      console.log(`[${monacoModelUri}]Starting Monaco Language Client...`);
      const runningLanguageClient = languageClient.start();
      monacoEditorRuntime.currentMonacoLanguageClientRunning = runningLanguageClient;
      console.log(`[${monacoModelUri}]Started Monaco Language Client`);

      connection.onClose(() => {
        console.log(`[${monacoModelUri}]Closing JSON-RPC connection...`);
        languageClient.stop();
        runningLanguageClient.dispose();
        monacoEditorRuntime.currentJsonRpcConnection = null;
        monacoEditorRuntime.currentMonacoLanguageClient = null;
        console.log(`[${monacoModelUri}]Closed JSON-RPC connection`);
      });
    },
  });

  return monacoEditorRuntime;
}

function configureMonacoForLanguage(dslName: string, dslKeywords: Set<string>): MonacoDslConfiguration {
  console.log("Configuring Monaco for DSL '" + dslName + "'.");

  // TODO: here we assume that Xtext-based DSLs always use the lower case language name as file extension.
  const dslExtension = '.' + dslName.toLowerCase();
  monaco.languages.register({
    id: dslName,
    extensions: [dslExtension],
    aliases: [dslName],
    mimetypes: ['text/' + dslName],
  });

  // Hack to get around https://github.com/microsoft/monaco-editor/issues/1008 provided by https://github.com/microsoft/monaco-editor/issues/1423
  interface MonarchLanguageConfiguration extends monaco.languages.IMonarchLanguage {
    keywords: string[];
  }

  // Register a tokens provider for the language
  monaco.languages.setMonarchTokensProvider(dslName, {
    tokenizer: {
      root: [[/[a-z_$][\w$]*/, { cases: { '@keywords': 'keyword' } }]],
    },
    keywords: Array.from(dslKeywords.values()),
  } as MonarchLanguageConfiguration);

  // Define a new theme that contains only rules that match this language
  const dslThemeName: string = dslName + 'Theme';
  monaco.editor.defineTheme(dslThemeName, {
    base: 'vs',
    inherit: false,
    rules: [{ token: 'keyword', fontStyle: 'bold', foreground: '#880a41' }],
    colors: {},
  });

  // install Monaco language client services
  const monacoServices = MonacoServices.install(monaco);

  return { monacoServices, dslName, dslThemeName, dslExtension };
}

function createMonacoLanguageClient(lspText: LspText, messageConnection: MessageConnection): MonacoLanguageClient {
  return new MonacoLanguageClient({
    name: `${lspText.label} ${lspText.languageName} Language Client [${lspText.id}]`,
    clientOptions: {
      // FIXME: hard-coded
      // use a language id as a document selector
      documentSelector: [lspText.languageName],
      // disable the default error handler
      errorHandler: {
        error: () => ErrorAction.Continue,
        closed: () => CloseAction.DoNotRestart,
      },
    },
    // create a language client connection from the JSON RPC connection on demand
    connectionProvider: {
      get: (errorHandler, closeHandler) => {
        const connection = createConnection(messageConnection, errorHandler, closeHandler);
        return Promise.resolve(connection);
      },
    },
  });
}

const lspTextEventSubscription = gql`
  subscription lspTextEvent($input: LspTextEventInput!) {
    lspTextEvent(input: $input) {
      __typename
      ... on SubscribersUpdatedEventPayload {
        ...subscribersUpdatedEventPayloadFragment
      }
      ... on LspTextRefreshedEventPayload {
        ...lspTextRefreshedEventPayloadFragment
      }
    }
  }
  ${subscribersUpdatedEventPayloadFragment}
  ${lspTextRefreshedEventPayloadFragment}
`;

export const LspTextWebSocketContainer = ({ editingContextId, representationId }: RepresentationComponentProps) => {
  const lspTextContainerDiv = useRef(null);
  const [{ value, context }, dispatch] = useMachine<LspTextWebSocketContainerContext, LspTextWebSocketContainerEvent>(
    lspTextWebSocketContainerMachine,
    {
      context: {
        lspTextId: representationId,
      },
    }
  );
  const { toast, lspTextWebSocketContainer } = value as SchemaValue;
  const { id, lspText, message } = context;

  const { error } = useSubscription<GQLLspTextEventSubscription>(lspTextEventSubscription, {
    variables: {
      input: {
        id,
        editingContextId,
        lspTextId: representationId,
      },
    },
    fetchPolicy: 'no-cache',
    onSubscriptionData: ({ subscriptionData }) => {
      const subscriptionEmittedResultEvent: SubscriptionEmittedResultEvent = {
        type: 'SUBSCRIPTION_EMITTED_RESULT',
        result: subscriptionData,
      };
      dispatch(subscriptionEmittedResultEvent);
    },
    onSubscriptionComplete: () => {
      const subscriptionCompletedEvent: SubscriptionCompletedEvent = {
        type: 'SUBSCRIPTION_COMPLETED',
      };
      dispatch(subscriptionCompletedEvent);
    },
  });
  const monacoDslConfiguration = useRef(null);
  const monacoEditorRuntime = useRef(null);
  const { wsOrigin } = useContext(ServerContext);
  console.log('LspTextWSC-usecontext-wsOrigin:' + wsOrigin);
  /**
   * The main effect.
   * It makes sure Monaco is configured for our DSL, and then either:
   * - Updates the Monaco text model if it already exists
   * - Creates the Monaco text model, disposes the previous Monaco Editor and creates a new one for
   */
  useEffect(() => {
    const parentDiv = lspTextContainerDiv.current;
    if (lspTextWebSocketContainer === 'ready') {
      if (parentDiv) {
        console.log('Component is ready and our parent div exists.');

       // Step 1: if not already configured for our DSL, configure Monaco.
        if (
          monacoDslConfiguration.current === null ||
          monacoDslConfiguration.current.dslName !== lspText.languageName
        ) {
          // TODO: when we implement syntax highlighting with LSP's semantic tokens we won't need these.
          const languageKeywords = new Set<string>();
          if (lspText.languageName === 'Statemachine') {
            languageKeywords.add('events');
            languageKeywords.add('end');
            languageKeywords.add('resetEvents');
            languageKeywords.add('commands');
            languageKeywords.add('state');
            languageKeywords.add('=>');
          } else if (lspText.languageName === 'SysML') {
            languageKeywords.add('id');
            languageKeywords.add('comment');
            languageKeywords.add('about');
            languageKeywords.add('doc');
            languageKeywords.add('rep');
            languageKeywords.add('language');
            languageKeywords.add('@');
            languageKeywords.add('metadata');
            languageKeywords.add('feature');
            languageKeywords.add(':>>');
            languageKeywords.add('redefines');
            languageKeywords.add('package');
            languageKeywords.add('filter');
            languageKeywords.add('alias');
            languageKeywords.add('for');
            languageKeywords.add('import');
            languageKeywords.add('all');
            languageKeywords.add('::');
            languageKeywords.add('**');
            languageKeywords.add('public');
            languageKeywords.add('private');
            languageKeywords.add('protected');
            languageKeywords.add(':>');
            languageKeywords.add('specializes');
            languageKeywords.add('ordered');
            languageKeywords.add('nonunique');
            languageKeywords.add('defined');
            languageKeywords.add('by');
            languageKeywords.add('subsets');
            languageKeywords.add('..');
            languageKeywords.add('bind');
            languageKeywords.add('as');
            languageKeywords.add('succession');
            languageKeywords.add('first');
            languageKeywords.add('then');
            languageKeywords.add('dependency');
            languageKeywords.add('from');
            languageKeywords.add('to');
            languageKeywords.add('abstract');
            languageKeywords.add('variation');
            languageKeywords.add('variant');
            languageKeywords.add('in');
            languageKeywords.add('out');
            languageKeywords.add('inout');
            languageKeywords.add('end');
            languageKeywords.add('ref');
            languageKeywords.add('default');
            languageKeywords.add('attribute');
            languageKeywords.add('def');
            languageKeywords.add('enum');
            languageKeywords.add('occurrence');
            languageKeywords.add('individual');
            languageKeywords.add('snapshot');
            languageKeywords.add('timeslice');
            languageKeywords.add('event');
            languageKeywords.add('then');
            languageKeywords.add('item');
            languageKeywords.add('part');
            languageKeywords.add('block');
            languageKeywords.add('port');
            languageKeywords.add('~');
            languageKeywords.add('connection');
            languageKeywords.add('assoc');
            languageKeywords.add('connect');
            languageKeywords.add('message');
            languageKeywords.add('of');
            languageKeywords.add('stream');
            languageKeywords.add('flow');
            languageKeywords.add('interface');
            languageKeywords.add('allocation');
            languageKeywords.add('allocate');
            languageKeywords.add('action');
            languageKeywords.add('perform');
            languageKeywords.add('action');
            languageKeywords.add('accept');
            languageKeywords.add('via');
            languageKeywords.add('send');
            languageKeywords.add('merge');
            languageKeywords.add('decide');
            languageKeywords.add('join');
            languageKeywords.add('fork');
            languageKeywords.add('then');
            languageKeywords.add('else');
            languageKeywords.add('state');
            languageKeywords.add('parallel');
            languageKeywords.add('entry');
            languageKeywords.add('do');
            languageKeywords.add('exit');
            languageKeywords.add('exhibit');
            languageKeywords.add('transition');
            languageKeywords.add('if');
            languageKeywords.add('calc');
            languageKeywords.add('return');
            languageKeywords.add('constraint');
            languageKeywords.add('assert');
            languageKeywords.add('not');
            languageKeywords.add('requirement');
            languageKeywords.add('subject');
            languageKeywords.add('assume');
            languageKeywords.add('require');
            languageKeywords.add('frame');
            languageKeywords.add('actor');
            languageKeywords.add('stakeholder');
            languageKeywords.add('satisfy');
            languageKeywords.add('concern');
            languageKeywords.add('case');
            languageKeywords.add('objective');
            languageKeywords.add('analysis');
            languageKeywords.add('verification');
            languageKeywords.add('verify');
            languageKeywords.add('use');
            languageKeywords.add('view');
            languageKeywords.add('include');
            languageKeywords.add('render');
            languageKeywords.add('expose');
            languageKeywords.add('viewpoint');
            languageKeywords.add('rendering');
          } else {
            // No keywords.
          }
          monacoDslConfiguration.current = configureMonacoForLanguage(lspText.languageName, languageKeywords);
        }

        // Step 2: if it does not already exist, create the Monaco editor.
        if (monacoEditorRuntime.current === null) {
          console.log(
            'Creating Monaco Editor for LspText ' + lspText.id + '(' + lspText.label + ')' + '- wsOrigin :' + wsOrigin
          );
          monacoEditorRuntime.current = createMonacoEditorRuntime(
            monacoDslConfiguration.current,
            parentDiv,
            editingContextId,
            lspText,
            wsOrigin
          );
        }

        // Step 3: create/update the model displayed by Monaco.
        const monacoModelUri: monaco.Uri = createMonacoModelUri(
          monacoDslConfiguration.current,
          editingContextId,
          lspText
        );
        const monacoModel: monaco.editor.ITextModel = monaco.editor.getModel(monacoModelUri);
        if (monacoModel) {
          // There is already a known model with that URI.
          // We want to update its contents if they have changed.
          if (monacoModel.getValue() !== lspText.contents) {
            console.log('Updating Monaco editor with new contents');
            monacoModel.setValue(lspText.contents);
          }
        } else {
          // There is no known model with that URI.
          console.log('Creating and setting up new Monaco model with URI ' + monacoModelUri);
          const model: monaco.editor.ITextModel = monaco.editor.createModel(
            lspText.contents,
            monacoDslConfiguration.current.dslName,
            monacoModelUri
          );
          monacoEditorRuntime.current.monacoEditor.setModel(model);
        }
      } else {
        console.log('Component is ready but parent div does not exist');
      }
    }

    // Step 4: cleanup the Monaco models upon closing the representation, and dispose the editor if no more models are loaded.
    return function cleanup() {
      console.log('Main Effect Cleanup');
      if (lspTextWebSocketContainer === 'ready') {
        // Cleanup is called while we are showing a textual representation
        // FIXME: here we depend upon a value we should not have access to,
        // But it is different from 'parentDiv' so I am not sure how we are supposed to access it.
        // eslint-disable-next-line react-hooks/exhaustive-deps
        if (lspTextContainerDiv.current === null) {
          // The textual representation is not shown anymore: we want to dispose of anything related to it.
          // if (monacoEditorRuntime.current.currentMonacoLanguageClientRunning !== null) {
          //   console.log('Disposing running language client');
          //   monacoEditorRuntime.current.currentMonacoLanguageClientRunning.dispose();
          // }
          // if (monacoEditorRuntime.current.currentMonacoLanguageClient !== null) {
          //   console.log('Stopping language client');
          //   monacoEditorRuntime.current.currentMonacoLanguageClient.stop();
          // }
          // if (monacoEditorRuntime.current.currentJsonRpcConnection !== null) {
          //   console.log('Ending and disposing JSON-RPC Connection');
          //   monacoEditorRuntime.current.currentJsonRpcConnection.end();
          //   monacoEditorRuntime.current.currentJsonRpcConnection.dispose();
          // }
          const monacoModelUri: monaco.Uri = createMonacoModelUri(
            monacoDslConfiguration.current,
            editingContextId,
            lspText
          );
          const monacoModel: monaco.editor.ITextModel = monaco.editor.getModel(monacoModelUri);
          monacoModel.dispose();
          // The WebSocket depends on the model so we have to close it as well.
          console.log('Closing WebSocket ' + monacoEditorRuntime.current.webSocket.url);
          monacoEditorRuntime.current.webSocket.close();
          monacoEditorRuntime.current = null;
        }
      }

      // We want to dispose the Monaco editor if we have no more models associated with it.
      if (monacoEditorRuntime.current && monaco.editor.getModels().length === 0) {
        console.log('Monaco models now empty: disposing the editor');
        monacoEditorRuntime.current.monacoEditor.dispose();
      }
    };
  }, [
    lspTextWebSocketContainer,
    lspTextContainerDiv,
    lspText,
    editingContextId,
    monacoDslConfiguration,
    monacoEditorRuntime,
    wsOrigin,
  ]);

  // Effect to display any error message in the snackbar.
  useEffect(() => {
    if (error) {
      const { message } = error;
      const showToastEvent: ShowToastEvent = { type: 'SHOW_TOAST', message };
      dispatch(showToastEvent);
    }
  }, [error, dispatch]);
  let content = null;

  if (lspTextWebSocketContainer === 'ready') {
    content = <div ref={lspTextContainerDiv} id="lsptext-container"></div>;
  } else if (lspTextWebSocketContainer === 'complete') {
    content = (
      <div id="lsptext-container">
        <Typography variant="h5" align="center" data-testid="lsptext-complete-message">
          The textual representation is not accessible anymore.
        </Typography>
      </div>
    );
  }

  return (
    <>
      {content}
      <Snackbar
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        open={toast === 'visible'}
        autoHideDuration={3000}
        onClose={() => dispatch({ type: 'HIDE_TOAST' } as HideToastEvent)}
        message={message}
        action={
          <IconButton
            size="small"
            aria-label="close"
            color="inherit"
            onClick={() => dispatch({ type: 'HIDE_TOAST' } as HideToastEvent)}>
            <CloseIcon fontSize="small" />
          </IconButton>
        }
        data-testid="error"
      />
    </>
  );
};
