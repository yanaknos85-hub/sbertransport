// TODO хорошо бы переименовать все draft-pilot сущности. и да - "speeach", именно так назвали эндпойнт...
export const DRAFT_PILOT = `/draft-pilot`;
export const SMART_SPEACH_INTEGRATION = `/smart-speeach-integration`;
export const DRAFT_PILOT_MESSAGES = `${DRAFT_PILOT}/messages`;
export const DRAFT_PILOT_AUDIO_MESSAGES = `${SMART_SPEACH_INTEGRATION}/audio-messages`;

export enum SessionStatus {
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  CLOSED = 'CLOSED',
}

export const SessionStatusNames: Record<SessionStatus, string> = {
  [SessionStatus.ACTIVE]: 'Активна',
  [SessionStatus.EXPIRED]: 'Истекла',
  [SessionStatus.CLOSED]: 'Закрыта',
} as const;

export enum ActionStatus {
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export enum MessageType {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  QUESTION = 'QUESTION',
}
