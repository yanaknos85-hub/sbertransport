// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
global.localStorage = localStorageMock as any;

// Mock window.location
const originalLocation = window.location;

describe('isTestMode', () => {
  let isTestMode: any;

  beforeEach(async () => {
    jest.resetModules();
    isTestMode = (await import('./isTestMode')).isTestMode;

    // Reset mocks
    localStorageMock.getItem.mockClear();

    // Reset window.location
    delete (window as any).location;
    (window as any).location = {
      origin: originalLocation.origin,
      href: originalLocation.href,
      protocol: originalLocation.protocol,
      host: originalLocation.host,
      hostname: originalLocation.hostname,
      port: originalLocation.port,
      pathname: originalLocation.pathname,
      search: originalLocation.search,
      hash: originalLocation.hash,
    };
  });

  afterEach(() => {
    (window as any).location = originalLocation;
  });

  describe('when localStorage has TEST_MODE = "true"', () => {
    beforeEach(() => {
      localStorageMock.getItem.mockReturnValue('true');
    });

    it('should return true for localhost', () => {
      (window as any).location = { origin: 'http://localhost:3000' };
      expect(isTestMode()).toBe(true);
    });
  });

  describe('when localStorage has TEST_MODE = "false"', () => {
    beforeEach(() => {
      localStorageMock.getItem.mockReturnValue('false');
    });

    it('should return false for non-localhost', () => {
      (window as any).location = { origin: 'https://example.com' };
      expect(isTestMode()).toBe(false);
    });
  });

  describe('when localStorage has no TEST_MODE (default behavior)', () => {
    beforeEach(() => {
      localStorageMock.getItem.mockReturnValue(null);
    });

    it('should return true for localhost', () => {
      (window as any).location = { origin: 'http://localhost:3000' };
      expect(isTestMode()).toBe(true);
    });

    it('should return true for http://localhost', () => {
      (window as any).location = { origin: 'http://localhost' };
      expect(isTestMode()).toBe(true);
    });

    it('should return false for non-localhost domain', () => {
      (window as any).location = { origin: 'https://example.com' };
      expect(isTestMode()).toBe(false);
    });

    it('should return false for production domain', () => {
      (window as any).location = { origin: 'https://app.example.com' };
      expect(isTestMode()).toBe(false);
    });
  });

  describe('edge cases', () => {
    it('should handle empty localStorage item', () => {
      localStorageMock.getItem.mockReturnValue('');
      (window as any).location = { origin: 'http://localhost:3000' };
      expect(isTestMode()).toBe(true);
    });

    it('should handle undefined localStorage item', () => {
      localStorageMock.getItem.mockReturnValue(undefined);
      (window as any).location = { origin: 'http://localhost:3000' };
      expect(isTestMode()).toBe(true);
    });
  });

  describe('different localhost formats', () => {
    it('should handle localhost with different ports', () => {
      localStorageMock.getItem.mockReturnValue(null);
      (window as any).location = { origin: 'http://localhost:8080' };
      expect(isTestMode()).toBe(true);
    });

    it('should handle localhost with https', () => {
      localStorageMock.getItem.mockReturnValue(null);
      (window as any).location = { origin: 'https://localhost:3000' };
      expect(isTestMode()).toBe(true);
    });
  });
});
