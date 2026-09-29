import { formatRubles, fromRubles, toRubles } from './MoneyUtils';

describe('fromRubles', () => {
  it('should convert rubles to kopecks', () => {
    expect(fromRubles(1)).toBe(100);
    expect(fromRubles(10)).toBe(1000);
    expect(fromRubles(100)).toBe(10000);
  });

  it('should handle decimal values', () => {
    expect(fromRubles(1.5)).toBe(150);
    expect(fromRubles(10.99)).toBe(1099);
    expect(fromRubles(0.5)).toBe(50);
    expect(fromRubles(0.01)).toBe(1);
  });

  it('should handle zero', () => {
    expect(fromRubles(0)).toBe(0);
  });

  it('should handle negative values', () => {
    expect(fromRubles(-1)).toBe(-100);
    expect(fromRubles(-10.5)).toBe(-1050);
  });

  it('should round properly', () => {
    expect(fromRubles(1.005)).toBe(100);
    expect(fromRubles(1.0051)).toBe(101);
  });
});

describe('toRubles', () => {
  describe('without fraction', () => {
    it('should convert kopecks to rubles', () => {
      expect(toRubles(100)).toBe(1);
      expect(toRubles(1000)).toBe(10);
      expect(toRubles(10000)).toBe(100);
    });

    it('should round kopecks to rubles', () => {
      expect(toRubles(150)).toBe(2);
      expect(toRubles(199)).toBe(2);
      expect(toRubles(101)).toBe(1);
      expect(toRubles(99)).toBe(1);
    });

    it('should handle zero', () => {
      expect(toRubles(0)).toBe(0);
    });

    it('should round negative values correctly', () => {
      expect(toRubles(-100)).toBe(-1);
      expect(toRubles(-150)).toBe(-1);
      expect(toRubles(-199)).toBe(-2);
    });

    it('should round properly', () => {
      expect(toRubles(149)).toBe(1);
      expect(toRubles(150)).toBe(2);
    });
  });

  describe('with fraction', () => {
    it('should return rubles with fraction', () => {
      expect(toRubles(150, true)).toBe(1.5);
      expect(toRubles(199, true)).toBe(1.99);
      expect(toRubles(101, true)).toBe(1.01);
    });

    it('should handle zero with fraction', () => {
      expect(toRubles(0, true)).toBe(0);
    });

    it('should handle negative values with fraction', () => {
      expect(toRubles(-150, true)).toBe(-1.5);
      expect(toRubles(-199, true)).toBe(-1.99);
    });

    it('should preserve exact fraction', () => {
      expect(toRubles(123, true)).toBe(1.23);
      expect(toRubles(5, true)).toBe(0.05);
      expect(toRubles(95, true)).toBe(0.95);
    });
  });
});

describe('formatRubles', () => {
  describe('with valid number', () => {
    it('should format positive rubles', () => {
      expect(formatRubles(1000)).toBe('1\xA0000,00\xA0₽');
      expect(formatRubles(100)).toBe('100,00\xA0₽');
      expect(formatRubles(1)).toBe('1,00\xA0₽');
      expect(formatRubles(0)).toBe('0,00\xA0₽');
    });

    it('should format negative rubles', () => {
      expect(formatRubles(-1000)).toBe('-1\xA0000,00\xA0₽');
      expect(formatRubles(-1)).toBe('-1,00\xA0₽');
    });

    it('should format decimal rubles', () => {
      expect(formatRubles(1000.5)).toBe('1\xA0000,50\xA0₽');
      expect(formatRubles(100.99)).toBe('100,99\xA0₽');
    });

    it('should format large numbers', () => {
      expect(formatRubles(1000000)).toBe('1\xA0000\xA0000,00\xA0₽');
      expect(formatRubles(1234567.89)).toBe('1\xA0234\xA0567,89\xA0₽');
    });
  });

  describe('with undefined or null', () => {
    it('should return default empty value for undefined', () => {
      expect(formatRubles(undefined)).toBe('');
    });

    it('should return default empty value for null', () => {
      expect(formatRubles(null)).toBe('');
    });

    it('should use custom empty value for undefined', () => {
      expect(formatRubles(undefined, 'N/A')).toBe('N/A');
    });

    it('should use custom empty value for null', () => {
      expect(formatRubles(null, 'N/A')).toBe('N/A');
    });
  });

  describe('with special numeric values', () => {
    it('should handle Infinity', () => {
      expect(formatRubles(Infinity)).toBe('');
    });

    it('should handle -Infinity', () => {
      expect(formatRubles(-Infinity)).toBe('');
    });

    it('should handle NaN', () => {
      expect(formatRubles(NaN)).toBe('');
    });
  });

  describe('custom empty value', () => {
    it('should use custom empty value', () => {
      expect(formatRubles(undefined, '-')).toBe('-');
      expect(formatRubles(null, '-')).toBe('-');
    });

    it('should use empty string as empty value', () => {
      expect(formatRubles(undefined, '')).toBe('');
    });
  });
});
