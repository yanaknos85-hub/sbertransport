/* eslint-disable @typescript-eslint/no-explicit-any */
import { searchSymbol } from './searchSymbol';

describe('searchSymbol', () => {
  test('должен находить совпадение в начале строки', () => {
    const result = searchSymbol('авт', { label: 'Автомобиль' });
    expect(result).toBe(true);
  });

  test('должен находить совпадение в середине строки', () => {
    const result = searchSymbol('том', { label: 'Автомобиль' });
    expect(result).toBe(true);
  });

  test('должен находить совпадение в конце строки', () => {
    const result = searchSymbol('иль', { label: 'Автомобиль' });
    expect(result).toBe(true);
  });

  test('должен находить совпадение без учета регистра', () => {
    const result = searchSymbol('АВТ', { label: 'автомобиль' });
    expect(result).toBe(true);
  });

  test('должен возвращать false при отсутствии совпадения', () => {
    const result = searchSymbol('такс', { label: 'Автомобиль' });
    expect(result).toBe(false);
  });

  test('должен возвращать true для пустой строки input', () => {
    const result = searchSymbol('', { label: 'Что-то' });
    expect(result).toBe(true);
  });

  test('должен возвращать false при undefined value', () => {
    const result = searchSymbol('что-то', undefined);
    expect(result).toBe(false);
  });

  test('должен возвращать false при null value', () => {
    const result = searchSymbol('что-то', null as unknown as any);
    expect(result).toBe(false);
  });

  test('должен возвращать false при отсутствии label', () => {
    const result = searchSymbol('что-то', {});
    expect(result).toBe(false);
  });

  test('должен работать с русскими буквами', () => {
    const result = searchSymbol('привет', { label: 'Привет мир' });
    expect(result).toBe(true);
  });

  test('должен работать с пробелами', () => {
    const result = searchSymbol('два слова', { label: 'два слова в фразе' });
    expect(result).toBe(true);
  });

  test('должен работать с частичным совпадением пробелов', () => {
    const result = searchSymbol('два', { label: 'два слова' });
    expect(result).toBe(true);
  });

  test('должен возвращать false для пустого label', () => {
    const result = searchSymbol('что-то', { label: '' });
    expect(result).toBe(false);
  });

  test('должен работать с пустыми строками input и label', () => {
    const result = searchSymbol('', { label: '' });
    expect(result).toBe(true);
  });
});
