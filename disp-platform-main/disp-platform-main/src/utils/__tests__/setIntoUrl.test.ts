import { setIntoUrl } from '../setIntoUrl';

describe('setIntoUrl', () => {
  describe('string values', () => {
    test('should append string value', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: 'John' };

      setIntoUrl(urlParams, obj, 'name');

      expect(urlParams.get('name')).toBe('John');
    });

    test('should append numeric string value', () => {
      const urlParams = new URLSearchParams();
      const obj = { age: 25 };

      setIntoUrl(urlParams, obj, 'age');

      expect(urlParams.get('age')).toBe('25');
    });

    test('should append boolean true value', () => {
      const urlParams = new URLSearchParams();
      const obj = { active: true };

      setIntoUrl(urlParams, obj, 'active');

      expect(urlParams.get('active')).toBe('true');
    });

    test('should append boolean false value', () => {
      const urlParams = new URLSearchParams();
      const obj = { hidden: false };

      setIntoUrl(urlParams, obj, 'hidden');

      expect(urlParams.get('hidden')).toBe('false');
    });

    test('should append zero value', () => {
      const urlParams = new URLSearchParams();
      const obj = { count: 0 };

      setIntoUrl(urlParams, obj, 'count');

      expect(urlParams.get('count')).toBe('0');
    });
  });

  describe('null and undefined values', () => {
    test('should not append null value', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: null };

      setIntoUrl(urlParams, obj, 'name');

      expect(urlParams.has('name')).toBe(false);
    });

    test('should not append undefined value', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: undefined };

      setIntoUrl(urlParams, obj, 'name');

      expect(urlParams.has('name')).toBe(false);
    });
  });

  describe('array values', () => {
    test('should append array values with key[]', () => {
      const urlParams = new URLSearchParams();
      const obj = { tags: ['tag1', 'tag2', 'tag3'] };

      setIntoUrl(urlParams, obj, 'tags');

      expect(urlParams.getAll('tags[]')).toEqual(['tag1', 'tag2', 'tag3']);
      expect(urlParams.has('tags')).toBe(false);
    });

    test('should append numeric array values with key[]', () => {
      const urlParams = new URLSearchParams();
      const obj = { ids: [1, 2, 3] };

      setIntoUrl(urlParams, obj, 'ids');

      expect(urlParams.getAll('ids[]')).toEqual(['1', '2', '3']);
    });

    test('should append boolean array values with key[]', () => {
      const urlParams = new URLSearchParams();
      const obj = { flags: [true, false] };

      setIntoUrl(urlParams, obj, 'flags');

      expect(urlParams.getAll('flags[]')).toEqual(['true', 'false']);
    });

    test('should append empty array with key[]', () => {
      const urlParams = new URLSearchParams();
      const obj = { items: [] };

      setIntoUrl(urlParams, obj, 'items');

      // Empty array should not append anything
      expect(urlParams.has('items[]')).toBe(false);
    });

    test('should append array with mixed types', () => {
      const urlParams = new URLSearchParams();
      const obj = { values: [1, 'two', true, false] };

      setIntoUrl(urlParams, obj, 'values');

      expect(urlParams.getAll('values[]')).toEqual(['1', 'two', 'true', 'false']);
    });

    test('should append array with null values', () => {
      const urlParams = new URLSearchParams();
      const obj = { values: [1, null, 2] };

      setIntoUrl(urlParams, obj, 'values');

      // null values should be converted to string "null"
      expect(urlParams.getAll('values[]')).toEqual(['1', 'null', '2']);
    });
  });

  describe('falsy values handling', () => {
    test('should not append empty string', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: '' };

      setIntoUrl(urlParams, obj, 'name');

      expect(urlParams.has('name')).toBe(false);
    });

    test('should not append NaN', () => {
      const urlParams = new URLSearchParams();
      const obj = { value: NaN };

      setIntoUrl(urlParams, obj, 'value');

      expect(urlParams.has('value')).toBe(false);
    });

    test('should not append false when checking truthy', () => {
      const urlParams = new URLSearchParams();
      const obj = { hidden: false };

      setIntoUrl(urlParams, obj, 'hidden');

      // false is falsy but should be appended per the logic
      expect(urlParams.get('hidden')).toBe('false');
    });
  });

  describe('object values', () => {
    test('should append object as string [object Object]', () => {
      const urlParams = new URLSearchParams();
      const obj = { data: { key: 'value' } };

      setIntoUrl(urlParams, obj, 'data');

      expect(urlParams.get('data')).toBe('[object Object]');
    });
  });

  describe('multiple keys', () => {
    test('should append multiple keys independently', () => {
      const urlParams = new URLSearchParams();
      const obj = {
        name: 'John',
        age: 25,
        active: true,
      };

      setIntoUrl(urlParams, obj, 'name');
      setIntoUrl(urlParams, obj, 'age');
      setIntoUrl(urlParams, obj, 'active');

      expect(urlParams.get('name')).toBe('John');
      expect(urlParams.get('age')).toBe('25');
      expect(urlParams.get('active')).toBe('true');
    });
  });

  describe('non-object input', () => {
    test('should handle null object', () => {
      const urlParams = new URLSearchParams();
      setIntoUrl(urlParams, null, 'name');

      expect(urlParams.has('name')).toBe(false);
    });

    test('should handle undefined object', () => {
      const urlParams = new URLSearchParams();
      setIntoUrl(urlParams, undefined, 'name');

      expect(urlParams.has('name')).toBe(false);
    });
  });

  describe('key not in object', () => {
    test('should not append when key does not exist', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: 'John' };

      setIntoUrl(urlParams, obj, 'nonExistentKey');

      expect(urlParams.has('nonExistentKey')).toBe(false);
    });
  });

  describe('complex scenarios', () => {
    test('should handle multiple arrays in same object', () => {
      const urlParams = new URLSearchParams();
      const obj = {
        tags: ['tag1', 'tag2'],
        categories: ['cat1', 'cat2'],
      };

      setIntoUrl(urlParams, obj, 'tags');
      setIntoUrl(urlParams, obj, 'categories');

      expect(urlParams.getAll('tags[]')).toEqual(['tag1', 'tag2']);
      expect(urlParams.getAll('categories[]')).toEqual(['cat1', 'cat2']);
    });

    test('should handle combination of arrays and scalars', () => {
      const urlParams = new URLSearchParams();
      const obj = {
        tags: ['tag1', 'tag2'],
        name: 'John',
        count: 0,
      };

      setIntoUrl(urlParams, obj, 'tags');
      setIntoUrl(urlParams, obj, 'name');
      setIntoUrl(urlParams, obj, 'count');

      expect(urlParams.getAll('tags[]')).toEqual(['tag1', 'tag2']);
      expect(urlParams.get('name')).toBe('John');
      expect(urlParams.get('count')).toBe('0');
    });

    test('should handle null array', () => {
      const urlParams = new URLSearchParams();
      const obj = { items: null };

      setIntoUrl(urlParams, obj, 'items');

      expect(urlParams.has('items[]')).toBe(false);
    });
  });

  describe('value type conversions', () => {
    test('should convert Date object to string', () => {
      const urlParams = new URLSearchParams();
      const date = new Date('2023-01-01');
      const obj = { date: date };

      setIntoUrl(urlParams, obj, 'date');

      expect(urlParams.get('date')).toBe(date.toString());
    });

    test('should convert symbol to string', () => {
      const urlParams = new URLSearchParams();
      const obj = { sym: Symbol('test') };

      setIntoUrl(urlParams, obj, 'sym');

      expect(urlParams.get('sym')).toBe('Symbol(test)');
    });
  });
});
