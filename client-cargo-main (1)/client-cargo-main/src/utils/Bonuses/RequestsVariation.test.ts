// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock moment to avoid date dependencies
jest.mock('moment', () => {
  const mockDate = new Date('2024-01-15');
  const mockMoment = (date?: any) => ({
    format: () => '15.01.2024',
  });
  mockMoment.utc = () => mockMoment();
  mockMoment.now = () => mockDate.getTime();
  return mockMoment;
});

// Mock shared constants
jest.mock('shared/constants/constants', () => ({
  emptySign: '-',
}));

describe('requestsVariation', () => {
  let requestsVariation: any;

  beforeEach(async () => {
    jest.resetModules();
    requestsVariation = (await import('./RequestsVariation')).requestsVariation;
  });

  describe('format updateTime', () => {
    it('should format updateTime to DD.MM.YYYY', () => {
      const request = {
        id: '123',
        sum: 10000,
        operation: 'DEPOSIT',
        status: 'DONE',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus added',
      };

      const result = requestsVariation(request);

      expect(result.updateTime).toBe('15.01.2024');
    });
  });

  describe('format sum for DEPOSIT', () => {
    it('should add + sign for DEPOSIT operation', () => {
      const request = {
        id: '123',
        sum: 10000,
        operation: 'DEPOSIT',
        status: 'DONE',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus added',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('+100 ₽');
    });

    it('should format sum with thousands separator for DEPOSIT', () => {
      const request = {
        id: '123',
        sum: 1234567,
        operation: 'DEPOSIT',
        status: 'DONE',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus added',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('+12\u00A0345,67 ₽');
    });
  });

  describe('format sum for SPEND', () => {
    it('should use emptySign (-) for SPEND operation', () => {
      const request = {
        id: '123',
        sum: 5000,
        operation: 'SPEND',
        status: 'RESERVED',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus spent',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('-50 ₽');
    });

    it('should format sum with thousands separator for SPEND', () => {
      const request = {
        id: '123',
        sum: 9876543,
        operation: 'SPEND',
        status: 'RESERVED',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus spent',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('-98\u00A0765,43 ₽');
    });
  });

  describe('preserve other fields', () => {
    it('should preserve all other fields from original request', () => {
      const request = {
        id: 'abc-123',
        sum: 10000,
        operation: 'DEPOSIT',
        status: 'DONE',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Bonus added',
      };

      const result = requestsVariation(request);

      expect(result.id).toBe('abc-123');
      expect(result.operation).toBe('DEPOSIT');
      expect(result.status).toBe('DONE');
      expect(result.reason).toBe('Bonus added');
    });
  });

  describe('zero sum', () => {
    it('should format zero sum correctly', () => {
      const request = {
        id: '123',
        sum: 0,
        operation: 'DEPOSIT',
        status: 'DONE',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Zero bonus',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('+0 ₽');
    });
  });

  describe('edge case - small sum', () => {
    it('should format small sum correctly', () => {
      const request = {
        id: '123',
        sum: 1,
        operation: 'SPEND',
        status: 'CANCELED',
        updateTime: '2024-01-15T10:30:00.000Z',
        reason: 'Small bonus',
      };

      const result = requestsVariation(request);

      expect(result.sum).toBe('-0,01 ₽');
    });
  });
});
