/**
 * @jest-environment jsdom
 */
import uuid from '../uuid';

describe('uuid', () => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

  test('should return a string type', () => {
    const result = uuid();

    expect(typeof result).toBe('string');
  });

  test('should return a valid UUID v4 format', () => {
    const result = uuid();

    expect(uuidRegex.test(result)).toBe(true);
  });

  test('should match RFC4122 pattern: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx', () => {
    const result = uuid();

    // UUID v4 format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
    // - Position 14 must be '4' (version)
    // - Position 19 must be one of 8, 9, a, b (variant)
    expect(result[14]).toBe('4');
    expect('89ab'.indexOf(result[19])).toBeGreaterThanOrEqual(0);
  });

  test('should generate different UUID on each call', () => {
    const result1 = uuid();
    const result2 = uuid();
    const result3 = uuid();

    expect(result1).not.toBe(result2);
    expect(result1).not.toBe(result3);
    expect(result2).not.toBe(result3);
  });

  test('should generate multiple unique UUIDs in a loop', () => {
    const results = new Set<string>();
    const iterations = 100;

    for (let i = 0; i < iterations; i++) {
      results.add(uuid());
    }

    expect(results.size).toBe(iterations);
  });

  test('should have correct length of 36 characters', () => {
    const result = uuid();

    expect(result.length).toBe(36);
  });

  test('should contain hyphens in correct positions', () => {
    const result = uuid();

    expect(result[8]).toBe('-');
    expect(result[13]).toBe('-');
    expect(result[18]).toBe('-');
    expect(result[23]).toBe('-');
  });

  test('should only contain valid hex characters and hyphens', () => {
    const result = uuid();

    // Remove hyphens and check if all remaining characters are valid hex
    const hexPart = result.replace(/-/g, '');
    expect(/^[0-9a-f]+$/.test(hexPart)).toBe(true);
  });

  test('should produce UUID with version 4 at position 14', () => {
    const result = uuid();

    // The version digit should always be '4' for UUID v4
    expect(result.charAt(14)).toBe('4');
  });

  test('should produce UUID with variant bits at position 19', () => {
    const result = uuid();

    // The variant bits should be 8, 9, a, or b for RFC4122
    const variant = result.charAt(19);
    expect(['8', '9', 'a', 'b'].includes(variant)).toBe(true);
  });
});
