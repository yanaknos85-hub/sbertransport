import * as tt from './io-ts';
import uuid from './uuid';

describe('uuid', () => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

  describe('format', () => {
    it('should return a string in UUID format (8-4-4-4-12)', () => {
      const result = uuid();

      expect(result).toMatch(uuidRegex);
    });

    it('should have version 4 bit set (4 in the version position)', () => {
      const result = uuid();

      // The version digit (4) is at position 14 (0-indexed)
      expect(result[14]).toBe('4');
    });

    it('should have variant bits set correctly (8, 9, a, or b in position 19)', () => {
      const result = uuid();

      // The variant digit is at position 19 (0-indexed)
      const variantDigit = result[19];
      expect(['8', '9', 'a', 'b']).toContain(variantDigit.toLowerCase());
    });

    it('should contain exactly 4 hyphens', () => {
      const result = uuid();

      const hyphenCount = (result.match(/-/g) || []).length;
      expect(hyphenCount).toBe(4);
    });

    it('should have total length of 36 characters', () => {
      const result = uuid();

      expect(result.length).toBe(36);
    });
  });

  describe('uniqueness', () => {
    it('should generate unique values on multiple calls', () => {
      const results = Array.from({ length: 100 }, () => uuid());

      // All UUIDs should be unique
      expect(new Set(results).size).toBe(100);
    });

    it('should generate different UUIDs in consecutive calls', () => {
      const uuid1 = uuid();
      const uuid2 = uuid();

      expect(uuid1).not.toBe(uuid2);
    });
  });

  describe('type', () => {
    it('should return a value assignable to tt.UUID', () => {
      const result = uuid();

      // The return type should be tt.UUID (branded string)
      expect(typeof result).toBe('string');
    });

    it('should produce valid UUID according to io-ts uuid codec', () => {
      const result = uuid();

      // Validate using the io-ts uuid codec
      const validation = tt.uuid.decode(result);
      expect(validation._tag).toBe('Right');
    });
  });

  describe('edge cases', () => {
    it('should consistently return valid UUIDs in rapid succession', () => {
      const results = Array.from({ length: 1000 }, () => uuid());

      // All should match the UUID regex
      results.forEach(uuidStr => {
        expect(uuidStr).toMatch(uuidRegex);
      });

      // All should be unique
      expect(new Set(results).size).toBe(1000);
    });
  });
});
