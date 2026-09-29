import { MIN_ANALYTICS_START_DATE } from 'constants/app.constants';
import { formatFormValue, disabledYears } from '../utils';

describe('formatFormValue', () => {
  test('should return undefined for undefined input', () => {
    expect(formatFormValue(undefined)).toBeUndefined();
  });

  test('should return original array for non-empty array input', () => {
    expect(formatFormValue(['a', 'b'])).toEqual(['a', 'b']);
  });

  test('should return undefined for empty array input', () => {
    expect(formatFormValue([])).toBeUndefined();
  });

  test('should handle mixed whitespace strings', () => {
    expect(formatFormValue('  test  ')).toEqual(['  test  ']);
  });
});

describe('disabledYears', () => {
  const currentYear = new Date().getFullYear();

  test('should disable years before MIN_ANALYTICS_START_DATE', () => {
    expect(disabledYears({ year: () => MIN_ANALYTICS_START_DATE - 1 } as moment.Moment)).toBe(true);
  });

  test('should allow years between MIN_ANALYTICS_START_DATE and current year', () => {
    expect(disabledYears({ year: () => MIN_ANALYTICS_START_DATE } as moment.Moment)).toBe(false);
    expect(disabledYears({ year: () => currentYear } as moment.Moment)).toBe(false);
  });

  test('should disable years after current year', () => {
    expect(disabledYears({ year: () => currentYear + 1 } as moment.Moment)).toBe(true);
  });
});
