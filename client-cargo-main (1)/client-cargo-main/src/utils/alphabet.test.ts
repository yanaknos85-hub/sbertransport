// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

describe('genCharArray', () => {
  let genCharArray: any;

  beforeEach(async () => {
    jest.resetModules();
    genCharArray = (await import('./alphabet')).genCharArray;
  });

  describe('basic ranges', () => {
    it('should generate array of characters from A to Z', () => {
      const result = genCharArray('A', 'Z');

      expect(result).toHaveLength(26);
      expect(result).toEqual([
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      ]);
    });

    it('should generate array of characters from a to z', () => {
      const result = genCharArray('a', 'z');

      expect(result).toHaveLength(26);
      expect(result).toEqual([
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      ]);
    });

    it('should generate array of characters from 0 to 9', () => {
      const result = genCharArray('0', '9');

      expect(result).toHaveLength(10);
      expect(result).toEqual(['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']);
    });
  });

  describe('custom ranges', () => {
    it('should generate array with single character', () => {
      const result = genCharArray('A', 'A');

      expect(result).toHaveLength(1);
      expect(result).toEqual(['A']);
    });

    it('should generate array for partial alphabet', () => {
      const result = genCharArray('C', 'G');

      expect(result).toHaveLength(5);
      expect(result).toEqual(['C', 'D', 'E', 'F', 'G']);
    });

    it('should generate array for lowercase letters', () => {
      const result = genCharArray('m', 'p');

      expect(result).toHaveLength(4);
      expect(result).toEqual(['m', 'n', 'o', 'p']);
    });
  });

  describe('edge cases', () => {
    it('should return empty array if start > end', () => {
      const result = genCharArray('Z', 'A');

      expect(result).toHaveLength(0);
      expect(result).toEqual([]);
    });

    it('should handle special characters', () => {
      const result = genCharArray('-', '/');

      expect(result).toHaveLength(3);
      expect(result).toEqual(['-', '.', '/']);
    });
  });

  describe('unicode support', () => {
    it('should handle Cyrillic characters', () => {
      const result = genCharArray('А', 'Е');

      expect(result).toHaveLength(6);
      expect(result).toEqual(['А', 'Б', 'В', 'Г', 'Д', 'Е']);
    });
  });
});
