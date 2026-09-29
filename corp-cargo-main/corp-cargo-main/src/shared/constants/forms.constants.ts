export const HISTORY_PUSH_DELAY = 500;

export const RequestType = {
  REGULAR: 'REGULAR',
  RELOCATION: 'RELOCATION',
  SINGLE: 'SINGLE',
} as const satisfies Record<string, string>;
