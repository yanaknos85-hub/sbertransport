import { StepsEnum } from '../steps/steps.enum';

describe('StepsEnum', () => {
  test('should have ShiftsStep with value "shifts"', () => {
    expect(StepsEnum.ShiftsStep).toBe('shifts');
  });

  test('should be a valid enum', () => {
    expect(typeof StepsEnum).toBe('object');
    expect(Object.keys(StepsEnum)).toContain('ShiftsStep');
  });
});
