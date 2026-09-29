/* eslint-disable @typescript-eslint/no-explicit-any */
import { getFiltersValues, saveFiltersValues } from '../filtersStorage';

describe('filtersStorage', () => {
  const originalLocalStorage = window.localStorage;

  const createMockStorage = () => {
    const storage: Record<string, string> = {};
    return {
      getItem: jest.fn((key: string) => storage[key] ?? null),
      setItem: jest.fn((key: string, value: string) => {
        storage[key] = value;
      }),
      removeItem: jest.fn((key: string) => {
        delete storage[key];
      }),
      clear: jest.fn(() => {
        Object.keys(storage).forEach(key => delete storage[key]);
      }),
    };
  };

  let mockLocalStorage: ReturnType<typeof createMockStorage>;

  beforeEach(() => {
    mockLocalStorage = createMockStorage();
    // Replace localStorage with mock
    Object.defineProperty(window, 'localStorage', {
      value: mockLocalStorage,
      writable: true,
    });
  });

  afterEach(() => {
    // Restore original localStorage
    Object.defineProperty(window, 'localStorage', {
      value: originalLocalStorage,
      writable: true,
    });
  });

  describe('getFiltersValues', () => {
    beforeEach(() => {
      // Clear storage before each test
      mockLocalStorage.clear();
    });

    it('should get existing filters from localStorage', () => {
      const mockFilters = { status: 'active', type: 'vehicle' };
      mockLocalStorage.setItem('test-filters-key', JSON.stringify(mockFilters));

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual(mockFilters);
      expect(mockLocalStorage.getItem).toHaveBeenCalledWith('test-filters-key');
    });

    it('should handle missing key and return empty object', () => {
      const result = getFiltersValues('non-existent-key');

      expect(result).toEqual({});
    });

    it('should handle invalid JSON in localStorage and return empty object', () => {
      mockLocalStorage.setItem('test-filters-key', 'invalid json {');

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual({});
    });

    it('should handle null value in localStorage and return empty object', () => {
      mockLocalStorage.getItem.mockReturnValue(null as any);

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual({});
    });

    it('should handle multiple filter keys', () => {
      const filters1 = { key1: 'value1' };
      const filters2 = { key2: 'value2' };

      mockLocalStorage.setItem('key1', JSON.stringify(filters1));
      mockLocalStorage.setItem('key2', JSON.stringify(filters2));

      const result1 = getFiltersValues('key1');
      const result2 = getFiltersValues('key2');

      expect(result1).toEqual(filters1);
      expect(result2).toEqual(filters2);
    });

    it('should handle empty string in localStorage and return empty object', () => {
      mockLocalStorage.setItem('test-filters-key', '');

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual({});
    });

    it('should handle filters with complex nested objects', () => {
      const mockFilters = {
        nested: { deep: { value: 'test' } },
        array: [1, 2, 3],
        boolean: true,
        nullValue: null,
      };
      mockLocalStorage.setItem('test-filters-key', JSON.stringify(mockFilters));

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual(mockFilters);
    });

    it('should handle filters with special characters', () => {
      const mockFilters = { 'key-with-dash': 'value', 'key.with.dot': 'value2' };
      mockLocalStorage.setItem('test-filters-key', JSON.stringify(mockFilters));

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual(mockFilters);
    });

    it('should handle filters with numeric keys', () => {
      const mockFilters = { 0: 'zero', 1: 'one' };
      mockLocalStorage.setItem('test-filters-key', JSON.stringify(mockFilters));

      const result = getFiltersValues('test-filters-key');

      expect(result).toEqual(mockFilters);
    });
  });

  describe('saveFiltersValues', () => {
    beforeEach(() => {
      // Clear storage before each test
      mockLocalStorage.clear();
    });

    it('should save new filters to localStorage', () => {
      const filters = { status: 'active', type: 'vehicle' };

      saveFiltersValues('test-filters-key', filters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify(filters)
      );
    });

    it('should merge with existing filters', () => {
      const existingFilters = { existingKey: 'existingValue' };
      const newFilters = { newKey: 'newValue' };

      mockLocalStorage.setItem('test-filters-key', JSON.stringify(existingFilters));
      saveFiltersValues('test-filters-key', newFilters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify({ existingKey: 'existingValue', newKey: 'newValue' })
      );
    });

    it('should handle empty filters object', () => {
      saveFiltersValues('test-filters-key', {});

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify({})
      );
    });

    it('should handle filters with complex nested objects', () => {
      const filters = {
        nested: { deep: { value: 'test' } },
        array: [1, 2, 3],
        boolean: true,
        nullValue: null,
      };

      saveFiltersValues('test-filters-key', filters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify(filters)
      );
    });

    it('should handle localStorage full error (should not throw)', () => {
      // Mock localStorage.setItem to throw QuotaExceededError
      const error = new Error('QuotaExceeded') as any;
      error.name = 'QuotaExceededError';
      mockLocalStorage.setItem.mockImplementationOnce(() => {
        throw error;
      });

      // Should not throw
      expect(() => {
        saveFiltersValues('test-filters-key', { key: 'value' });
      }).not.toThrow();
    });

    it('should handle other localStorage errors gracefully', () => {
      mockLocalStorage.setItem.mockImplementationOnce(() => {
        throw new Error('Some other error');
      });

      // Should not throw
      expect(() => {
        saveFiltersValues('test-filters-key', { key: 'value' });
      }).not.toThrow();
    });

    it('should merge filters correctly with existing and new', () => {
      const existingFilters = { a: 1, b: 2 };
      const newFilters = { b: 3, c: 4 }; // b should be overridden

      mockLocalStorage.setItem('test-filters-key', JSON.stringify(existingFilters));
      saveFiltersValues('test-filters-key', newFilters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify({
          a: 1, b: 3, c: 4,
        })
      );
    });

    it('should save filters with special characters', () => {
      const filters = { 'key-with-dash': 'value', 'key.with.dot': 'value2' };

      saveFiltersValues('test-filters-key', filters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify(filters)
      );
    });

    it('should handle undefined filters parameter', () => {
      saveFiltersValues(
        'test-filters-key',
        undefined as unknown as Record<string, unknown>
      );

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify({})
      );
    });

    it('should overwrite existing key with new value', () => {
      const initialFilters = { key: 'initial' };
      const updatedFilters = { key: 'updated' };

      mockLocalStorage.setItem('test-filters-key', JSON.stringify(initialFilters));
      saveFiltersValues('test-filters-key', updatedFilters);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'test-filters-key',
        JSON.stringify(updatedFilters)
      );
    });
  });

  describe('integration', () => {
    beforeEach(() => {
      // Clear storage before each test
      mockLocalStorage.clear();
    });

    it('should persist and retrieve filters correctly', () => {
      const initialFilters = {
        status: 'active', type: 'vehicle', limit: 10,
      };

      // Save filters
      saveFiltersValues('integration-test', initialFilters);

      // Retrieve filters
      const retrievedFilters = getFiltersValues('integration-test');

      expect(retrievedFilters).toEqual(initialFilters);
    });

    it('should handle multiple save and retrieve operations', () => {
      // First save
      saveFiltersValues('multi-test', { step: 1 });
      expect(getFiltersValues('multi-test')).toEqual({ step: 1 });

      // Second save (merge)
      saveFiltersValues('multi-test', { step: 2 });
      expect(getFiltersValues('multi-test')).toEqual({ step: 2 });

      // Third save (add new key)
      saveFiltersValues('multi-test', { additional: 'value' });
      expect(getFiltersValues('multi-test')).toEqual({
        step: 2,
        additional: 'value',
      });
    });
  });
});
