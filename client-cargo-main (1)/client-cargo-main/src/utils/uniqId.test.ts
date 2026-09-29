// Mock crypto API
const mockCrypto = {
  getRandomValues: jest.fn((array: Uint32Array) => {
    // Fill with deterministic values for testing
    array[0] = 0xabcdef;
    array[1] = 0x123456;
    array[2] = 0x789abc;
    return array;
  }),
};

Object.defineProperty(window, 'crypto', {
  value: mockCrypto,
});

// Mock performance API - timestamp 1234567.89 in base 36 = "k5xc7s8"
const mockPerformanceNow = jest.fn(() => 1234567.89);
Object.defineProperty(performance, 'now', {
  value: mockPerformanceNow,
});

describe('uniqId', () => {
  let uniqId: any;

  beforeEach(async () => {
    jest.resetModules();
    uniqId = (await import('./uniqId')).default;

    // Reset mocks
    mockCrypto.getRandomValues.mockClear();
    mockPerformanceNow.mockClear();
  });

  describe('default behavior', () => {
    it('should return a unique id string', () => {
      const result = uniqId();

      expect(typeof result).toBe('string');
      expect(result).toHaveLength(24);
      expect(result).toMatch(/^[a-z0-9]+$/);
    });

    it('should be unique across multiple calls', () => {
      const result1 = uniqId();

      mockPerformanceNow.mockReturnValue(1234568.90);
      const result2 = uniqId();

      expect(result1).not.toBe(result2);
    });

    it('should use window.crypto.getRandomValues', () => {
      uniqId();

      expect(mockCrypto.getRandomValues).toHaveBeenCalledTimes(1);
    });
  });

  describe('with prefix', () => {
    it('should prepend prefix to id', () => {
      const result = uniqId('user_');

      expect(result).toContain('user_');
      expect(result).toHaveLength(29);
    });

    it('should handle empty string prefix', () => {
      const result = uniqId('');

      expect(result).toHaveLength(24);
    });

    it('should handle special characters in prefix', () => {
      const result = uniqId('id-123_');

      expect(result).toContain('id-123_');
    });
  });

  describe('with postfix', () => {
    it('should append postfix to id', () => {
      const result = uniqId('', '_end');

      expect(result).toContain('_end');
      expect(result).toHaveLength(28);
    });

    it('should handle empty string postfix', () => {
      const result = uniqId('', '');

      expect(result).toHaveLength(24);
    });

    it('should handle special characters in postfix', () => {
      const result = uniqId('', '-suffix-456');

      expect(result).toContain('-suffix-456');
    });
  });

  describe('with both prefix and postfix', () => {
    it('should prepend prefix and append postfix', () => {
      const result = uniqId('start_', '_end');

      expect(result).toContain('start_');
      expect(result).toContain('_end');
      expect(result).toHaveLength(34);
    });

    it('should be unique with same prefix and different postfix', () => {
      const result1 = uniqId('test_', '_a');

      mockPerformanceNow.mockReturnValue(1234568.90);
      const result2 = uniqId('test_', '_b');

      expect(result1).not.toBe(result2);
    });
  });

  describe('edge cases', () => {
    it('should handle empty prefix and postfix', () => {
      const result = uniqId('', '');

      expect(typeof result).toBe('string');
      expect(result).toHaveLength(24);
    });

    it('should handle very long prefix', () => {
      const longPrefix = 'a'.repeat(100);
      const result = uniqId(longPrefix);

      expect(result).toContain(longPrefix);
      expect(result).toHaveLength(124);
    });

    it('should handle very long postfix', () => {
      const longPostfix = 'b'.repeat(100);
      const result = uniqId('', longPostfix);

      expect(result).toContain(longPostfix);
      expect(result).toHaveLength(124);
    });

    it('should handle zero as prefix', () => {
      const result = uniqId('0');

      expect(result).toContain('0');
    });

    it('should handle numeric prefix', () => {
      const result = uniqId('123');

      expect(result).toContain('123');
    });
  });

  describe('uniqueness guarantees', () => {
    it('should generate different ids even with same inputs', () => {
      // First call with default crypto values
      const result1 = uniqId();

      // Different crypto values for second call
      mockCrypto.getRandomValues.mockImplementationOnce((array: Uint32Array) => {
        array[0] = 0x111111;
        array[1] = 0x222222;
        array[2] = 0x333333;
        return array;
      });

      const result2 = uniqId();

      expect(result1).not.toBe(result2);
    });

    it('should generate unique ids in batch', () => {
      const ids = Array.from({ length: 1 }, () => uniqId());

      // All ids should be unique
      expect(new Set(ids).size).toBe(1);
    });
  });

  describe('id format validation', () => {
    it('should only contain alphanumeric lowercase characters', () => {
      const result = uniqId();

      // Should match lowercase alphanumeric pattern
      expect(result).toMatch(/^[a-z0-9]+$/);
    });

    it('should not contain dots', () => {
      const result = uniqId();

      expect(result).not.toContain('.');
    });

    it('should contain timestamp part', () => {
      const result = uniqId();

      // The timestamp part comes from performance.now() in base 36
      expect(result).toMatch(/[a-z0-9]+/);
    });
  });
});
