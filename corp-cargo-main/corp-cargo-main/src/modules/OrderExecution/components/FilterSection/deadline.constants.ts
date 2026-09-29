export const DeadlineType = {
  LESS_THAN_20: 'LESS_THAN_20',
  BETWEEN_20_AND_50: 'BETWEEN_20_AND_50',
  MORE_THAN_50: 'MORE_THAN_50',
} as const satisfies Record<string, string>;

export type DeadlineValue = (typeof DeadlineType)[keyof typeof DeadlineType];

export const DEADLINE_OPTION_WIDTHS: Record<DeadlineValue, number> = {
  [DeadlineType.LESS_THAN_20]: 200,
  [DeadlineType.BETWEEN_20_AND_50]: 225,
  [DeadlineType.MORE_THAN_50]: 235,
};

export const DEFAULT_SELECT_WIDTH = 190;

export const getSelectWidth = (value: DeadlineValue | null | undefined): number => {
  if (!value) return DEFAULT_SELECT_WIDTH;
  return DEADLINE_OPTION_WIDTHS[value] ?? DEFAULT_SELECT_WIDTH;
};