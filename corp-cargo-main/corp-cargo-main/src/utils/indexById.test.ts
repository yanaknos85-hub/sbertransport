import indexById, { indexByName } from './indexById';

describe('indexById', () => {
  test('для массива из одного элемента должен вернуть объект с ключом id', () => {
    const input: { id: string; name: string }[] = [{ id: '1', name: 'Item 1' }];
    expect(indexById(input)).toEqual({ '1': { id: '1', name: 'Item 1' } });
  });

  test('для массива из нескольких элементов должен создать индекс по id', () => {
    const input: { id: string; name: string }[] = [
      { id: '1', name: 'Item 1' },
      { id: '2', name: 'Item 2' },
      { id: '3', name: 'Item 3' },
    ];
    expect(indexById(input)).toEqual({
      '1': { id: '1', name: 'Item 1' },
      '2': { id: '2', name: 'Item 2' },
      '3': { id: '3', name: 'Item 3' },
    });
  });

  test('для массива с дублирующимися id должен оставить последний элемент', () => {
    const input: { id: string; name: string }[] = [
      { id: '1', name: 'Item 1' },
      { id: '1', name: 'Item 1 Updated' },
    ];
    expect(indexById(input)).toEqual({
      '1': { id: '1', name: 'Item 1 Updated' },
    });
  });

  test('для пустого массива должен вернуть пустой объект', () => {
    expect(indexById([])).toEqual({});
  });

  test('для массива объектов без лишних полей должен сохранить все поля', () => {
    const input: { id: string; name: string; value: number; active: boolean }[] = [
      { id: 'a', name: 'A', value: 100, active: true },
      { id: 'b', name: 'B', value: 200, active: false },
    ];
    expect(indexById(input)).toEqual({
      a: { id: 'a', name: 'A', value: 100, active: true },
      b: { id: 'b', name: 'B', value: 200, active: false },
    });
  });

  test('для массива объектов с id как пустая строка должен использовать "" как ключ', () => {
    const input: { id: string; name: string }[] = [
      { id: '', name: 'Item 1' },
      { id: '2', name: 'Item 2' },
    ];
    expect(indexById(input)).toEqual({
      '': { id: '', name: 'Item 1' },
      '2': { id: '2', name: 'Item 2' },
    });
  });
});

describe('indexByName', () => {
  test('для массива из одного элемента должен вернуть объект с ключом name', () => {
    const input: { name: string; id: string }[] = [{ name: 'Item 1', id: '1' }];
    expect(indexByName(input)).toEqual({ 'Item 1': { name: 'Item 1', id: '1' } });
  });

  test('для массива из нескольких элементов должен создать индекс по name', () => {
    const input: { name: string; id: string }[] = [
      { name: 'Item 1', id: '1' },
      { name: 'Item 2', id: '2' },
    ];
    expect(indexByName(input)).toEqual({
      'Item 1': { name: 'Item 1', id: '1' },
      'Item 2': { name: 'Item 2', id: '2' },
    });
  });

  test('для массива с дублирующимися name должен оставить последний элемент', () => {
    const input: { name: string; id: string }[] = [
      { name: 'Item 1', id: '1' },
      { name: 'Item 1', id: '2' },
    ];
    expect(indexByName(input)).toEqual({
      'Item 1': { name: 'Item 1', id: '2' },
    });
  });

  test('для пустого массива должен вернуть пустой объект', () => {
    expect(indexByName([])).toEqual({});
  });
});
