import { preparePhoneForBackend } from '../preparePhoneForBackend';

describe('preparePhoneForBackend', () => {
  describe('onlyNumbers pattern', () => {
    test('should format valid phone with +7 prefix', () => {
      expect(preparePhoneForBackend('+7 999 123-45-67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should format valid phone with 8 prefix', () => {
      expect(preparePhoneForBackend('8 999 123-45-67', 'onlyNumbers')).toBe('+89991234567');
    });

    test('should format phone with spaces only', () => {
      expect(preparePhoneForBackend('+7 999 123 45 67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should format phone with dashes only', () => {
      expect(preparePhoneForBackend('+7-999-123-45-67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should format phone with mixed separators', () => {
      expect(preparePhoneForBackend('+7 (999) 123-45-67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone with parentheses', () => {
      expect(preparePhoneForBackend('+7(999)1234567', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone with dots as separators', () => {
      expect(preparePhoneForBackend('+7.999.123-45-67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone with no separators', () => {
      expect(preparePhoneForBackend('+79991234567', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone starting with 8 instead of +7', () => {
      expect(preparePhoneForBackend('89991234567', 'onlyNumbers')).toBe('+89991234567');
    });

    test('should add + prefix when missing', () => {
      expect(preparePhoneForBackend('79991234567', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle empty string', () => {
      expect(preparePhoneForBackend('', 'onlyNumbers')).toBe('+');
    });

    test('should handle string with only non-digits', () => {
      expect(preparePhoneForBackend('abcdef', 'onlyNumbers')).toBe('+');
    });

    test('should handle null as empty string', () => {
      // @ts-expect-error - testing edge case
      expect(preparePhoneForBackend(null, 'onlyNumbers')).toBe('+');
    });

    test('should handle undefined as empty string', () => {
      // @ts-expect-error - testing edge case
      expect(preparePhoneForBackend(undefined, 'onlyNumbers')).toBe('+');
    });

    test('should handle very long phone with extra digits', () => {
      expect(preparePhoneForBackend('+7 999 123 45 67 890123', 'onlyNumbers')).toBe('+79991234567890123');
    });

    test('should handle phone with leading zeros', () => {
      expect(preparePhoneForBackend('+7 099 123 45 67', 'onlyNumbers')).toBe('+70991234567');
    });

    test('should handle international format with country code', () => {
      expect(preparePhoneForBackend('+44 20 7946 0958', 'onlyNumbers')).toBe('+442079460958');
    });

    test('should handle US phone format', () => {
      expect(preparePhoneForBackend('+1 (555) 123-4567', 'onlyNumbers')).toBe('+15551234567');
    });

    test('should handle phone with only country code', () => {
      expect(preparePhoneForBackend('+7', 'onlyNumbers')).toBe('+7');
    });

    test('should preserve leading + when already present', () => {
      expect(preparePhoneForBackend('+79991234567', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle string with letters and numbers', () => {
      expect(preparePhoneForBackend('+7abc999def123ghi456jkl789', 'onlyNumbers')).toBe('+7999123456789');
    });
  });

  describe('withBrackets pattern', () => {
    test('should format valid phone with brackets', () => {
      expect(preparePhoneForBackend('+7 999 123-45-67', 'withBrackets')).toBe('+7(999)1234567');
    });

    test('should format valid phone with spaces', () => {
      expect(preparePhoneForBackend('+7 999 123 45 67', 'withBrackets')).toBe('+7(999)1234567');
    });

    test('should format phone with dashes', () => {
      expect(preparePhoneForBackend('+7-999-123-45-67', 'withBrackets')).toBe('+7(999)1234567');
    });

    test('should format phone with parentheses', () => {
      expect(preparePhoneForBackend('+7 (999) 123-45-67', 'withBrackets')).toBe('+7(999)1234567');
    });

    test('should format phone with mixed separators', () => {
      expect(preparePhoneForBackend('+7 (999) 123-45 67', 'withBrackets')).toBe('+7(999)1234567');
    });

    test('should handle phone starting with 8', () => {
      expect(preparePhoneForBackend('8 999 123-45-67', 'withBrackets')).toBe('+89991234567');
    });

    test('should handle phone without country code', () => {
      expect(preparePhoneForBackend('999 123-45-67', 'withBrackets')).toBe('+9991234567');
    });

    test('should handle empty string', () => {
      expect(preparePhoneForBackend('', 'withBrackets')).toBe('+');
    });

    test('should handle string with only non-digits', () => {
      expect(preparePhoneForBackend('abcdef', 'withBrackets')).toBe('+');
    });

    test('should format very long phone', () => {
      expect(preparePhoneForBackend('+7 999 123 45 67 890123', 'withBrackets')).toBe('+7(999)1234567890123');
    });

    test('should format phone with leading zeros', () => {
      expect(preparePhoneForBackend('+7 099 123 45 67', 'withBrackets')).toBe('+7(099)1234567');
    });

    test('should handle international format', () => {
      expect(preparePhoneForBackend('+44 20 7946 0958', 'withBrackets')).toBe('+442079460958');
    });

    test('should handle US phone format', () => {
      expect(preparePhoneForBackend('+1 (555) 123-4567', 'withBrackets')).toBe('+15551234567');
    });

    test('should handle phone with only country code', () => {
      expect(preparePhoneForBackend('+7', 'withBrackets')).toBe('+7');
    });
  });

  describe('default pattern (onlyNumbers)', () => {
    test('should use onlyNumbers as default when pattern not specified', () => {
      expect(preparePhoneForBackend('+7 999 123-45-67')).toBe('+79991234567');
    });

    test('should work with onlyNumbers as default pattern', () => {
      expect(preparePhoneForBackend('+7 999 123 45 67')).toBe('+79991234567');
    });
  });

  describe('edge cases', () => {
    test('should handle string with leading plus followed by non-digit', () => {
      expect(preparePhoneForBackend('+abc', 'onlyNumbers')).toBe('+');
    });

    test('should handle multiple plus signs', () => {
      expect(preparePhoneForBackend('++79991234567', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle string with only plus sign', () => {
      expect(preparePhoneForBackend('+', 'onlyNumbers')).toBe('+');
    });

    test('should handle string with minus signs only', () => {
      expect(preparePhoneForBackend('---', 'onlyNumbers')).toBe('+');
    });

    test('should handle phone with leading spaces', () => {
      expect(preparePhoneForBackend('   +7 999 123-45-67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone with trailing spaces', () => {
      expect(preparePhoneForBackend('+7 999 123-45-67   ', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle phone with tabs and newlines', () => {
      expect(preparePhoneForBackend('+7\t999\n123 45 67', 'onlyNumbers')).toBe('+79991234567');
    });

    test('should handle unicode digits', () => {
      // Full-width digits should be stripped (not recognized as digits)
      expect(preparePhoneForBackend('+７９９１２３４５６７', 'onlyNumbers')).toBe('+');
    });
  });

  describe('pattern comparison', () => {
    test('should produce different results for different patterns', () => {
      const phone = '+7 999 123-45-67';
      const onlyNumbers = preparePhoneForBackend(phone, 'onlyNumbers');
      const withBrackets = preparePhoneForBackend(phone, 'withBrackets');

      expect(onlyNumbers).not.toBe(withBrackets);
      expect(onlyNumbers).toBe('+79991234567');
      expect(withBrackets).toBe('+7(999)1234567');
    });

    test('should handle same phone with both patterns', () => {
      const phone = '+7 900 111 22 33';

      expect(preparePhoneForBackend(phone, 'onlyNumbers')).toBe('+79001112233');
      expect(preparePhoneForBackend(phone, 'withBrackets')).toBe('+7(900)1112233');
    });
  });
});
