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
import IconButton from '@material-ui/core/IconButton';
import Snackbar from '@material-ui/core/Snackbar';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import { useMachine } from '@xstate/react';
import gql from 'graphql-tag';
import React, { useEffect } from 'react';
import { RepresentationComponentProps } from 'workbench/Workbench.types';
import { GQLLspTextEventSubscription } from './LspTextWebSocketContainer.types';
import {
  HandleCompleteEvent,
  HandleSubscriptionResultEvent,
  HideToastEvent,
  LspTextWebSocketContainerContext,
  LspTextWebSocketContainerEvent,
  lspTextWebSocketContainerMachine,
  SchemaValue,
  ShowToastEvent,
} from './LspTextWebSocketContainerMachine';

const lspTextEventSubscription = gql`
  subscription lspTextEvent($input: LspTextEventInput!) {
    lspTextEvent(input: $input) {
      __typename
      ... on LspTextRefreshedEventPayload {
        id
        lspText {
          id
          label
          contents
        }
      }
    }
  }
`;

export const LspTextWebSocketContainer = ({ editingContextId, representationId }: RepresentationComponentProps) => {
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
      const handleDataEvent: HandleSubscriptionResultEvent = {
        type: 'HANDLE_SUBSCRIPTION_RESULT',
        result: subscriptionData,
      };
      dispatch(handleDataEvent);
    },
    onSubscriptionComplete: () => {
      const completeEvent: HandleCompleteEvent = { type: 'HANDLE_COMPLETE' };
      dispatch(completeEvent);
    },
  });

  useEffect(() => {
    if (error) {
      const { message } = error;
      const showToastEvent: ShowToastEvent = { type: 'SHOW_TOAST', message };
      dispatch(showToastEvent);
    }
  }, [error, dispatch]);

  let content = null;
  if (lspTextWebSocketContainer === 'ready') {
    content = (
      <div id="lsptext-container">
        <TextField
          name={lspText.label}
          placeholder={'The textual representation is empty.'}
          value={lspText.contents}
          margin="dense"
          multiline
          rowsMax={20}
          fullWidth
        />
      </div>
    );
  } else if (lspTextWebSocketContainer === 'complete') {
    content = (
      <div id="lsptext-container">
        <Typography variant="h5" align="center" data-testid="lsptext-complete-message">
          The textual representation does not exist.
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
