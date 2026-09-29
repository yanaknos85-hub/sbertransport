import type { MutationResultPair } from 'react-query';
import type { AxiosError } from 'axios';

import { useAPIMutation } from 'api';
import { DRAFT_PILOT_MESSAGES } from './draft-pilot.constants';
import {
  TUserMessageRequest,
  TUserMessageResponse
} from './draft-pilot.types';
import { ignore } from 'utils';

export const DRAFT_PILOT_MESSAGES_KEY = 'draftPilotMessagesKey';

declare module 'api' {
  interface Cache {
    postDraftPilotMessage: {
      key: [typeof DRAFT_PILOT_MESSAGES_KEY, TUserMessageRequest];
      value: TUserMessageResponse;
    };
  }
}

const DRAFT_PILOT_TIMEOUT = 1000 * 60 * 3; // По просьбе бэка временно очень большой таймаут. После тестирования или уменьшить, или перевести на вебсокеты

/**
 * Отправка сообщения в ассистент Draft Pilot.
 * Первое сообщение — без sessionId (null), последующие — с существующей сессией.
 */
export const useSendDraftPilotMessage = (): MutationResultPair<
  TUserMessageResponse,
  AxiosError<Error>,
  TUserMessageRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<TUserMessageResponse>(DRAFT_PILOT_MESSAGES, data, { timeout: DRAFT_PILOT_TIMEOUT })
      .then(process.getResponseData)
  ), {
    onSuccess: ignore,
  })
);
