import indexById, { indexByName } from '../indexById';

describe('indexById', () => {
  it('превращает массив объектов в словарь, проиндексированный по id', () => {
    const input = [
      { id: 'a', value: 1 },
      { id: 'b', value: 2 },
    ];
    expect(indexById(input)).toEqual({
      a: { id: 'a', value: 1 },
      b: { id: 'b', value: 2 },
    });
  });

  it('возвращает пустой объект для пустого массива', () => {
    expect(indexById([])).toEqual({});
  });

  it('сохраняет оригинальные ссылки на значения', () => {
    const a = { id: 'a', value: 1 };
    const b = { id: 'b', value: 2 };
    const result = indexById([a, b]);

    expect(result.a).toBe(a);
    expect(result.b).toBe(b);
  });

  it('при дубликатах id последний элемент перезаписывает предыдущие', () => {
    const first = { id: 'a', value: 1 };
    const second = { id: 'a', value: 2 };

    const result = indexById([first, second]);

    expect(result).toEqual({ a: second });
  });

  it('сохраняет порядок ключей как в исходном массиве', () => {
    const result = indexById([
      { id: 'z', value: 1 },
      { id: 'a', value: 2 },
      { id: 'm', value: 3 },
    ]);

    expect(Object.keys(result)).toEqual(['z', 'a', 'm']);
  });
});

describe('indexByName', () => {
  it('превращает массив в словарь, проиндексированный по name', () => {
    const input = [
      {
        id: '1', name: 'foo', value: 10,
      },
      {
        id: '2', name: 'bar', value: 20,
      },
    ];

    expect(indexByName(input)).toEqual({
      foo: {
        id: '1', name: 'foo', value: 10,
      },
      bar: {
        id: '2', name: 'bar', value: 20,
      },
    });
  });

  it('возвращает пустой объект для пустого массива', () => {
    expect(indexByName([])).toEqual({});
  });

  it('сохраняет оригинальные ссылки на значения', () => {
    const a = { id: '1', name: 'foo' };
    const result = indexByName([a]);

    expect(result.foo).toBe(a);
  });
});
