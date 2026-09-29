import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { ActionStatus, MessageType } from './smart-speeach-integration.constants';

const ActionStatuses = ioTypeFromEnum<ActionStatus>('ActionStatus', ActionStatus);
const DraftPilotMessageType = ioTypeFromEnum<MessageType>('MessageType', MessageType);

export const ApiErrorDto = t.type({
  code: t.string,
  message: t.string,
  traceId: tt.uuid,
  details: t.union([t.UnknownRecord, t.undefined]),
});
export type TApiErrorDto = t.TypeOf<typeof ApiErrorDto>;

export const MessageDto = t.type({
  type: DraftPilotMessageType,
  text: t.string,
  recognizedText: t.union([t.string, t.undefined]),
  alternatives: t.union([t.array(t.string), t.undefined]),
});
export type TMessageDto = t.TypeOf<typeof MessageDto>;

export const DataResponse = t.type({
  traceId: tt.uuid,
});
export type TDataResponse = t.TypeOf<typeof DataResponse>;

/**
 * Ответ на аудио-сообщение.
 * Расширяет standard-ответ полем recognizedText (обязательное) и alternatives (опциональное).
 */
export const AudioMessageResponse = t.type({
  data: DataResponse,
  message: MessageDto,
  action: ActionStatuses,
});
export type TAudioMessageResponse = t.TypeOf<typeof AudioMessageResponse>;
