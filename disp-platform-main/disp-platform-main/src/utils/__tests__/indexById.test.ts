import indexById from '../indexById';

describe('indexById', () => {
  test('should create index from array of objects by id', () => {
    const items = [
      { id: '1', name: 'Item 1' },
      { id: '2', name: 'Item 2' },
      { id: '3', name: 'Item 3' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      1: { id: '1', name: 'Item 1' },
      2: { id: '2', name: 'Item 2' },
      3: { id: '3', name: 'Item 3' },
    });
  });

  test('should work with empty array', () => {
    const result = indexById([]);

    expect(result).toEqual({});
  });

  test('should work with single item', () => {
    const item = { id: 'item-1', value: 'test' };

    const result = indexById([item]);

    expect(result).toEqual({
      'item-1': { id: 'item-1', value: 'test' },
    });
  });

  test('should preserve original objects (reference equality)', () => {
    const item1 = {
      id: '1', name: 'Item 1', data: { nested: 'value' },
    };
    const item2 = { id: '2', name: 'Item 2' };

    const result = indexById([item1, item2]);

    expect(result['1']).toBe(item1);
    expect(result['2']).toBe(item2);
  });

  test('should handle duplicate ids (last wins)', () => {
    const items = [
      { id: '1', name: 'First' },
      { id: '1', name: 'Second' },
      { id: '1', name: 'Third' },
    ];

    const result = indexById(items);

    expect(result['1']).toEqual({ id: '1', name: 'Third' });
  });

  test('should work with objects containing nested structures', () => {
    const items = [
      {
        id: 'parent-1',
        children: [
          { id: 'child-1', name: 'Child 1' },
          { id: 'child-2', name: 'Child 2' },
        ],
        metadata: { created: '2024-01-01' },
      },
      {
        id: 'parent-2',
        children: [],
        metadata: { created: '2024-01-02' },
      },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      'parent-1': items[0],
      'parent-2': items[1],
    });
  });

  test('should work with objects having numeric-like ids', () => {
    const items = [
      { id: '0', value: 'zero' },
      { id: '123', value: 'number' },
      { id: '007', value: 'string' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      '0': { id: '0', value: 'zero' },
      '123': { id: '123', value: 'number' },
      '007': { id: '007', value: 'string' },
    });
  });

  test('should work with complex custom types', () => {
    interface Vehicle {
      id: string;
      brand: string;
      model: string;
      year: number;
    }

    const vehicles: Vehicle[] = [
      {
        id: 'v1', brand: 'Toyota', model: 'Camry', year: 2020,
      },
      {
        id: 'v2', brand: 'BMW', model: 'X5', year: 2021,
      },
    ];

    const result = indexById(vehicles);

    expect(result).toEqual({
      v1: {
        id: 'v1', brand: 'Toyota', model: 'Camry', year: 2020,
      },
      v2: {
        id: 'v2', brand: 'BMW', model: 'X5', year: 2021,
      },
    });
  });

  test('should handle special characters in ids', () => {
    const items = [
      { id: 'id-with-dash', value: 'dash' },
      { id: 'id_with_underscore', value: 'underscore' },
      { id: 'id.with.dots', value: 'dots' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      'id-with-dash': { id: 'id-with-dash', value: 'dash' },
      'id_with_underscore': { id: 'id_with_underscore', value: 'underscore' },
      'id.with.dots': { id: 'id.with.dots', value: 'dots' },
    });
  });

  test('should handle Cyrillic ids', () => {
    const items = [
      { id: 'айди-1', name: 'Товар 1' },
      { id: 'айди-2', name: 'Товар 2' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      'айди-1': { id: 'айди-1', name: 'Товар 1' },
      'айди-2': { id: 'айди-2', name: 'Товар 2' },
    });
  });

  test('should work with large arrays', () => {
    const items = Array.from({ length: 100 }, (_, i) => ({
      id: `item-${i}`,
      index: i,
    }));

    const result = indexById(items);

    expect(Object.keys(result).length).toBe(100);
    expect(result['item-0']).toEqual({ id: 'item-0', index: 0 });
    expect(result['item-99']).toEqual({ id: 'item-99', index: 99 });
  });

  test('should handle ids with whitespace', () => {
    const items = [
      { id: 'id with space', value: 'space' },
      { id: 'id\twith\ttab', value: 'tab' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      'id with space': { id: 'id with space', value: 'space' },
      'id\twith\ttab': { id: 'id\twith\ttab', value: 'tab' },
    });
  });

  test('should work with boolean values in objects', () => {
    const items = [
      {
        id: '1', enabled: true, active: false,
      },
      {
        id: '2', enabled: false, active: true,
      },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      1: {
        id: '1', enabled: true, active: false,
      },
      2: {
        id: '2', enabled: false, active: true,
      },
    });
  });

  test('should work with null and undefined values in objects', () => {
    const items = [
      { id: '1', value: null },
      { id: '2', value: undefined },
      { id: '3', value: 'defined' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      1: { id: '1', value: null },
      2: { id: '2', value: undefined },
      3: { id: '3', value: 'defined' },
    });
  });

  test('should work with empty string ids', () => {
    const items = [
      { id: '', value: 'empty-id' },
      { id: 'normal', value: 'normal-id' },
    ];

    const result = indexById(items);

    expect(result).toEqual({
      '': { id: '', value: 'empty-id' },
      'normal': { id: 'normal', value: 'normal-id' },
    });
  });
});
