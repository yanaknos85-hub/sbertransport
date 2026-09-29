// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock moment to avoid date dependencies
jest.mock('moment', () => {
  const mockDate = new Date('2024-01-15T10:30:00.000Z');
  const mockMoment = (date?: any) => ({
    utc: () => ({
      format: () => '2024-01-15T10:30:00Z',
    }),
    local: () => ({
      format: () => '2024-01-15T13:30:00+03:00',
    }),
    format: () => '2024-01-15T10:30:00Z',
  });
  mockMoment.utc = (date?: any) => mockMoment(date);
  mockMoment.now = () => mockDate.getTime();
  return mockMoment;
});

describe('datetime utils', () => {
  let localTimeStringToUtcString: any;
  let utcTimeStringToLocalMoment: any;

  beforeEach(async () => {
    jest.resetModules();
    const datetime = await import('./datetime');
    localTimeStringToUtcString = datetime.localTimeStringToUtcString;
    utcTimeStringToLocalMoment = datetime.utcTimeStringToLocalMoment;
  });

  describe('localTimeStringToUtcString', () => {
    it('should convert local time string to UTC string', () => {
      const localTime = '2024-01-15T13:30:00+03:00';
      const result = localTimeStringToUtcString(localTime);

      expect(result).toBe('2024-01-15T10:30:00Z');
    });

    it('should convert ISO local time string to UTC', () => {
      const localTime = '2024-01-15T10:30:00';
      const result = localTimeStringToUtcString(localTime);

      expect(result).toBe('2024-01-15T10:30:00Z');
    });

    it('should handle date-only string', () => {
      const localTime = '2024-01-15';
      const result = localTimeStringToUtcString(localTime);

      expect(result).toBe('2024-01-15T10:30:00Z');
    });
  });

  describe('utcTimeStringToLocalMoment', () => {
    it('should convert UTC time string to local moment', () => {
      const utcTime = '2024-01-15T10:30:00+00:00';
      const result = utcTimeStringToLocalMoment(utcTime);

      expect(result.format()).toBe('2024-01-15T13:30:00+03:00');
    });

    it('should handle empty string', () => {
      const result = utcTimeStringToLocalMoment('');

      expect(result.format()).toBe('2024-01-15T13:30:00+03:00');
    });

    it('should handle undefined', () => {
      const result = utcTimeStringToLocalMoment();

      expect(result.format()).toBe('2024-01-15T13:30:00+03:00');
    });
  });

  describe('edge cases', () => {
    it('should handle Unix timestamp', () => {
      const timestamp = 1705315800000;
      const result = localTimeStringToUtcString(String(timestamp));

      expect(result).toBe('2024-01-15T10:30:00Z');
    });
  });
});
