// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

describe('formatDistance', () => {
  let formatDistance: any;

  beforeEach(async () => {
    jest.resetModules();
    const DistanceUtils = await import('./DistanceUtils');
    formatDistance = DistanceUtils.formatDistance;
  });

  describe('rounding behavior', () => {
    it('should round 1.2 to 1 km', () => {
      expect(formatDistance(1.2)).toBe('1 км');
    });

    it('should round 1.5 to 2 km', () => {
      expect(formatDistance(1.5)).toBe('2 км');
    });

    it('should round 1.8 to 2 km', () => {
      expect(formatDistance(1.8)).toBe('2 км');
    });

    it('should round 0.5 to 1 km', () => {
      expect(formatDistance(0.5)).toBe('1 км');
    });

    it('should round 0.4 to 0 km', () => {
      expect(formatDistance(0.4)).toBe('0 км');
    });

    it('should handle negative numbers', () => {
      expect(formatDistance(-1.5)).toBe('-1 км');
    });
  });

  describe('large distances', () => {
    it('should format 100 km', () => {
      expect(formatDistance(100)).toBe('100 км');
    });

    it('should format 1000 km', () => {
      expect(formatDistance(1000)).toBe('1000 км');
    });

    it('should format 1234 km', () => {
      expect(formatDistance(1234.56)).toBe('1235 км');
    });

    it('should format 10000 km', () => {
      expect(formatDistance(10000)).toBe('10000 км');
    });
  });

  describe('edge cases', () => {
    it('should handle zero', () => {
      expect(formatDistance(0)).toBe('0 км');
    });

    it('should handle very small number', () => {
      expect(formatDistance(0.01)).toBe('0 км');
    });

    it('should handle very large number', () => {
      expect(formatDistance(999999.99)).toBe('1000000 км');
    });
  });

  describe('format', () => {
    it('should include space and km', () => {
      expect(formatDistance(5)).toBe('5 км');
    });

    it('should include space and km for decimal', () => {
      expect(formatDistance(5.7)).toBe('6 км');
    });
  });
});
