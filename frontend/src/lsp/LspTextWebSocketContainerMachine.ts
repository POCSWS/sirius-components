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

import { SubscriptionResult } from '@apollo/client';
import { v4 as uuid } from 'uuid';
import { assign, Machine } from 'xstate';
import {
  GQLLspTextEventPayload,
  GQLLspTextEventSubscription,
  GQLLspTextRefreshedEventPayload,
  LspText,
} from './LspTextWebSocketContainer.types';

export interface LspTextWebSocketContainerContext {
  id: string;
  lspTextId: string;
  lspText: LspText | null;
  message: string | null;
}

export interface LspTextWebSocketContainerStateSchema {
  states: {
    toast: {
      states: {
        visible: {};
        hidden: {};
      };
    };
    lspTextWebSocketContainer: {
      states: {
        idle: {};
        ready: {};
        complete: {};
      };
    };
  };
}

export type SchemaValue = {
  toast: 'visible' | 'hidden';
  lspTextWebSocketContainer: 'idle' | 'ready' | 'complete';
};

export type ShowToastEvent = {
  type: 'SHOW_TOAST';
  message: string;
};
export type HideToastEvent = {
  type: 'HIDE_TOAST';
};

export type SubscriptionEmittedResultEvent = {
  type: 'SUBSCRIPTION_EMITTED_RESULT';
  result: SubscriptionResult<GQLLspTextEventSubscription>;
};
export type SubscriptionCompletedEvent = {
  type: 'SUBSCRIPTION_COMPLETED';
};

export type LspTextWebSocketContainerEvent =
  | SubscriptionEmittedResultEvent
  | SubscriptionCompletedEvent
  | ShowToastEvent
  | HideToastEvent;

export const lspTextWebSocketContainerMachine = Machine<
  LspTextWebSocketContainerContext,
  LspTextWebSocketContainerStateSchema,
  LspTextWebSocketContainerEvent
>(
  {
    type: 'parallel',
    context: {
      id: uuid(),
      lspTextId: null,
      lspText: null,
      message: null,
    },
    states: {
      toast: {
        initial: 'hidden',
        states: {
          hidden: {
            on: {
              SHOW_TOAST: {
                target: 'visible',
                actions: 'setMessage',
              },
            },
          },
          visible: {
            on: {
              HIDE_TOAST: {
                target: 'hidden',
                actions: 'clearMessage',
              },
            },
          },
        },
      },
      lspTextWebSocketContainer: {
        initial: 'idle',
        states: {
          idle: {
            on: {
              SUBSCRIPTION_EMITTED_RESULT: [
                {
                  cond: 'isLspTextRefreshedEventPayload',
                  target: 'ready',
                  actions: 'handleSubscriptionResult',
                },
                {
                  target: 'idle',
                  actions: 'handleSubscriptionResult',
                },
              ],
            },
          },
          ready: {
            on: {
              SUBSCRIPTION_EMITTED_RESULT: [
                {
                  cond: 'isLspTextRefreshedEventPayload',
                  target: 'ready',
                  actions: 'handleSubscriptionResult',
                },
              ],
              SUBSCRIPTION_COMPLETED: {
                target: 'complete',
                actions: 'handleSubscriptionCompleted',
              },
            },
          },
          complete: {
            type: 'final',
          },
        },
      },
    },
  },
  {
    guards: {
      isLspTextRefreshedEventPayload: (_, event) => {
        const { result } = event as SubscriptionEmittedResultEvent;
        const { data } = result;
        return isLspTextRefreshedEventPayload(data.lspTextEvent);
      },
    },
    actions: {
      handleSubscriptionResult: assign((_, event) => {
        const { result } = event as SubscriptionEmittedResultEvent;
        const { data } = result;
        if (isLspTextRefreshedEventPayload(data.lspTextEvent)) {
          const { lspText } = data.lspTextEvent;

          const processedLspText: LspText = {
            id: lspText.id,
            label: lspText.label,
            contents: lspText.contents,
            languageName: lspText.languageName,
          };

          return {
            lspText: processedLspText,
            message: null,
          };
        }
        return {
          message: 'Received invalid refresh event',
        };
      }),
      handleSubscriptionCompleted: assign((_, event) => {
        console.log('Subscription completed');
        return {};
      }),

      setMessage: assign((_, event) => {
        const { message } = event as ShowToastEvent;
        return {
          message,
        };
      }),
      clearMessage: assign((_) => {
        return {
          message: null,
        };
      }),
    },
  }
);

const isLspTextRefreshedEventPayload = (payload: GQLLspTextEventPayload): payload is GQLLspTextRefreshedEventPayload =>
  payload.__typename === 'LspTextRefreshedEventPayload';
