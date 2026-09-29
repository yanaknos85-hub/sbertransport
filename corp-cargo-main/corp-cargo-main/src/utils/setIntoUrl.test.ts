import { setIntoUrl } from './setIntoUrl';

describe('setIntoUrl', () => {
  describe('обработка массивов', () => {
    test('должен добавить значения массива с суффиксом []', () => {
      const urlParams = new URLSearchParams();
      const obj = { tags: ['tag1', 'tag2', 'tag3'] };

      setIntoUrl(urlParams, obj, 'tags');

      expect(urlParams.getAll('tags[]')).toEqual(['tag1', 'tag2', 'tag3']);
    });

    test('должен добавить пустой массив без добавления значений', () => {
      const urlParams = new URLSearchParams();
      const obj = { items: [] };

      setIntoUrl(urlParams, obj, 'items');

      expect(urlParams.has('items[]')).toBe(false);
      expect(urlParams.toString()).toBe('');
    });

    test('должен добавить массив с одним элементом', () => {
      const urlParams = new URLSearchParams();
      const obj = { ids: [42] };

      setIntoUrl(urlParams, obj, 'ids');

      expect(urlParams.getAll('ids[]')).toEqual(['42']);
    });

    test('должен добавить массив с разными типами значений', () => {
      const urlParams = new URLSearchParams();
      const obj = { values: ['str', 123, true, false] };

      setIntoUrl(urlParams, obj, 'values');

      expect(urlParams.getAll('values[]')).toEqual(['str', '123', 'true', 'false']);
    });

    test('должен добавить массив с нулями', () => {
      const urlParams = new URLSearchParams();
      const obj = { counts: [0, 1, 0] };

      setIntoUrl(urlParams, obj, 'counts');

      expect(urlParams.getAll('counts[]')).toEqual(['0', '1', '0']);
    });

    test('должен добавить массив с null', () => {
      const urlParams = new URLSearchParams();
      const obj = { values: [null, 'test', null] };

      setIntoUrl(urlParams, obj, 'values');

      expect(urlParams.getAll('values[]')).toEqual(['null', 'test', 'null']);
    });
  });

  describe('обработка скалярных значений', () => {
    test('должен добавить строковое значение', () => {
      const urlParams = new URLSearchParams();
      const obj = { name: 'testValue' };

      setIntoUrl(urlParams, obj, 'name');

      expect(urlParams.get('name')).toBe('testValue');
    });

    test('должен добавить числовое значение', () => {
      const urlParams = new URLSearchParams();
      const obj = { page: 5 };

      setIntoUrl(urlParams, obj, 'page');

      expect(urlParams.get('page')).toBe('5');
    });

    test('должен добавить ноль', () => {
      const urlParams = new URLSearchParams();
      const obj = { count: 0 };

      setIntoUrl(urlParams, obj, 'count');

      expect(urlParams.get('count')).toBe('0');
    });

    test('должен добавить true', () => {
      const urlParams = new URLSearchParams();
      const obj = { active: true };

      setIntoUrl(urlParams, obj, 'active');

      expect(urlParams.get('active')).toBe('true');
    });

    test('должен добавить false', () => {
      const urlParams = new URLSearchParams();
      const obj = { disabled: false };

      setIntoUrl(urlParams, obj, 'disabled');

      expect(urlParams.get('disabled')).toBe('false');
    });

    test('должен преобразовать объект в строку', () => {
      const urlParams = new URLSearchParams();
      const obj = { data: { key: 'value' } };

      setIntoUrl(urlParams, obj, 'data');

      expect(urlParams.get('data')).toBe('[object Object]');
    });

    test('должен преобразовать символ в строку', () => {
      const urlParams = new URLSearchParams();
      const obj = { symbol: Symbol('test') };

      setIntoUrl(urlParams, obj, 'symbol');

      expect(urlParams.get('symbol')).toBe('Symbol(test)');
    });
  });

  describe('обработка null/undefined', () => {
    test('не должен добавлять undefined', () => {
      const urlParams = new URLSearchParams();
      const obj = { value: undefined };

      setIntoUrl(urlParams, obj, 'value');

      expect(urlParams.has('value')).toBe(false);
      expect(urlParams.toString()).toBe('');
    });

    test('не должен добавлять null', () => {
      const urlParams = new URLSearchParams();
      const obj = { value: null };

      setIntoUrl(urlParams, obj, 'value');

      expect(urlParams.has('value')).toBe(false);
      expect(urlParams.toString()).toBe('');
    });

    test('не должен добавлять отсутствующий ключ', () => {
      const urlParams = new URLSearchParams();
      const obj = { existing: 'value' };

      setIntoUrl(urlParams, obj, 'nonExisting');

      expect(urlParams.has('nonExisting')).toBe(false);
      expect(urlParams.toString()).toBe('');
    });

    test('не должен добавлять пустую строку', () => {
      const urlParams = new URLSearchParams();
      const obj = { value: '' };

      setIntoUrl(urlParams, obj, 'value');

      expect(urlParams.has('value')).toBe(false);
      expect(urlParams.toString()).toBe('');
    });
  });

  describe('поведение URLSearchParams', () => {
    test('должен использовать append для множественных значений', () => {
      const urlParams = new URLSearchParams();
      urlParams.append('existing', 'value1');

      const obj = { tags: ['tag1', 'tag2'] };

      setIntoUrl(urlParams, obj, 'tags');

      expect(urlParams.getAll('existing')).toEqual(['value1']);
      expect(urlParams.getAll('tags[]')).toEqual(['tag1', 'tag2']);
    });

    test('должен добавлять к уже существующим параметрам', () => {
      const urlParams = new URLSearchParams();
      urlParams.append('filter', 'existing');

      const obj = { filter: 'new' };

      setIntoUrl(urlParams, obj, 'filter');

      expect(urlParams.getAll('filter')).toEqual(['existing', 'new']);
    });
  });
});
