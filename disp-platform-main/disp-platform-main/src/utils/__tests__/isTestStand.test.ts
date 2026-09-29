/**
 * @jest-environment jsdom
 */
import { isTestStand } from '../isTestStand';

describe('isTestStand', () => {
  const originalLocation = window.location;

  beforeEach(() => {
    Object.defineProperty(window, 'location', {
      value: { origin: '' },
      writable: true,
    });
  });

  afterEach(() => {
    // @ts-ignore
    window.location = originalLocation;
  });

  test('should return true for localhost', () => {
    // @ts-ignore
    window.location.origin = 'http://localhost:3000';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for localhost with port', () => {
    // @ts-ignore
    window.location.origin = 'https://localhost:8080';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for .platform. domain', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for .taxi. domain', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.taxi.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for .cargo. domain', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.cargo.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for .fleet. domain', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.fleet.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for subdomain containing .platform.', () => {
    // @ts-ignore
    window.location.origin = 'https://test.platform.sber.ru';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for subdomain containing .taxi.', () => {
    // @ts-ignore
    window.location.origin = 'https://test.taxi.sber.ru';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for subdomain containing .cargo.', () => {
    // @ts-ignore
    window.location.origin = 'https://test.cargo.sber.ru';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for subdomain containing .fleet.', () => {
    // @ts-ignore
    window.location.origin = 'https://test.fleet.sber.ru';
    expect(isTestStand()).toBe(true);
  });

  test('should return false for production domain without prefixes', () => {
    // @ts-ignore
    window.location.origin = 'https://example.com';
    expect(isTestStand()).toBe(false);
  });

  test('should return false for production domain with different subdomain', () => {
    // @ts-ignore
    window.location.origin = 'https://prod.example.com';
    expect(isTestStand()).toBe(false);
  });

  test('should return false for production domain with different subdomain pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://app.example.com';
    expect(isTestStand()).toBe(false);
  });

  test('should return false for staging domain without recognized pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://staging.example.com';
    expect(isTestStand()).toBe(false);
  });

  test('should return false for production subdomain without pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://production.example.com';
    expect(isTestStand()).toBe(false);
  });

  test('should handle HTTPS protocol', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should handle HTTP protocol', () => {
    // @ts-ignore
    window.location.origin = 'http://localhost:3000';
    expect(isTestStand()).toBe(true);
  });

  test('should handle domain with platform in path but not as prefix', () => {
    // @ts-ignore
    window.location.origin = 'https://example.com/platform';
    expect(isTestStand()).toBe(false);
  });

  test('should handle domain ending with platform', () => {
    // @ts-ignore
    window.location.origin = 'https://example.platform';
    expect(isTestStand()).toBe(false);
  });

  test('should handle very long domain with platform', () => {
    // @ts-ignore
    window.location.origin = 'https://deeply.nested.dev.platform.sberbank.ru';
    expect(isTestStand()).toBe(true);
  });

  test('should handle single character domain with platform', () => {
    // @ts-ignore
    window.location.origin = 'https://a.platform.b';
    expect(isTestStand()).toBe(true);
  });

  test('should handle domain with multiple platform occurrences', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.test.platform.example.com';
    expect(isTestStand()).toBe(true);
  });

  test('should return true for taxi subdomain pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://taxi.sber.ru';
    expect(isTestStand()).toBe(false);
  });

  test('should return true for cargo subdomain pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://cargo.sber.ru';
    expect(isTestStand()).toBe(false);
  });

  test('should return true for fleet subdomain pattern', () => {
    // @ts-ignore
    window.location.origin = 'https://fleet.sber.ru';
    expect(isTestStand()).toBe(false);
  });

  test('should handle origin with trailing slash', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.example.com/';
    expect(isTestStand()).toBe(true);
  });

  test('should handle origin with path', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.example.com/some/path';
    expect(isTestStand()).toBe(true);
  });

  test('should handle origin with query params', () => {
    // @ts-ignore
    window.location.origin = 'https://dev.platform.example.com?param=value';
    expect(isTestStand()).toBe(true);
  });

  test('should handle mixed scheme (https:// vs http://)', () => {
    // @ts-ignore
    window.location.origin = 'http://localhost';
    expect(isTestStand()).toBe(true);

    // @ts-ignore
    window.location.origin = 'https://localhost';
    expect(isTestStand()).toBe(true);
  });

  describe('edge cases', () => {
    test('should handle empty origin', () => {
      // @ts-ignore
      window.location.origin = '';
      expect(isTestStand()).toBe(false);
    });

    test('should handle origin with only protocol', () => {
      // @ts-ignore
      window.location.origin = 'https://';
      expect(isTestStand()).toBe(false);
    });

    test('should handle very long origin with platform', () => {
      const longDomain = 'a'.repeat(100) + '.platform.' + 'b'.repeat(100);
      // @ts-ignore
      window.location.origin = `https://${longDomain}`;
      expect(isTestStand()).toBe(true);
    });

    test('should handle origin with special characters', () => {
      // @ts-ignore
      window.location.origin = 'https://dev.platform.test-123.example.com';
      expect(isTestStand()).toBe(true);
    });
  });
});
