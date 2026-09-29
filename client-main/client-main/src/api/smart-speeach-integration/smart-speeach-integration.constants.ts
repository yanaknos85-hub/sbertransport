export const SMART_SPEACH_INTEGRATION = `/smart-speeach-integration`;
export const SMART_SPEACH_INTEGRATION_AUDIO_MESSAGES = `${SMART_SPEACH_INTEGRATION}/audio-messages`;

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
