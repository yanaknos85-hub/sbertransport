import { getJoinedArrayAsString } from './arrayHelpers';

describe('getJoinedArrayAsString', () => {
  test('для массива строк должен вернуть строку с разделителем по умолчанию ";"', () => {
    expect(getJoinedArrayAsString(['a', 'b', 'c'])).toBe('a;b;c');
  });

  test('для массива чисел должен вернуть строку с разделителем по умолчанию ";"', () => {
    expect(getJoinedArrayAsString([1, 2, 3])).toBe('1;2;3');
  });

  test('для массива смешанных строк и чисел должен вернуть строку с разделителем по умолчанию ";"', () => {
    expect(getJoinedArrayAsString(['a', 1, 'b', 2])).toBe('a;1;b;2');
  });

  test('для массива с кастомным разделителем должен использовать его', () => {
    expect(getJoinedArrayAsString(['a', 'b', 'c'], ',')).toBe('a,b,c');
  });

  test('для массива с кастомным разделителем "-" должен вернуть правильную строку', () => {
    expect(getJoinedArrayAsString([1, 2, 3], '-')).toBe('1-2-3');
  });

  test('для пустого массива должен вернуть undefined', () => {
    expect(getJoinedArrayAsString([])).toBeUndefined();
  });

  test('для undefined должен вернуть undefined', () => {
    expect(getJoinedArrayAsString(undefined)).toBeUndefined();
  });

  test('для массива из одного элемента должен вернуть этот элемент как строку', () => {
    expect(getJoinedArrayAsString(['single'])).toBe('single');
  });

  test('для массива из одного числового элемента должен вернуть это число как строку', () => {
    expect(getJoinedArrayAsString([42])).toBe('42');
  });

  test('для массива с пустыми строками должен учитывать их', () => {
    expect(getJoinedArrayAsString(['', 'b', ''])).toBe(';b;');
  });

  test('для массива с нулями должен учитывать их', () => {
    expect(getJoinedArrayAsString([0, 1, 0])).toBe('0;1;0');
  });

  test('для массива с false должен учитывать его как элемент', () => {
    expect(getJoinedArrayAsString([false as unknown as string, 'b'])).toBe('false;b');
  });

  test('для массива с null должен преобразовывать его в пустую строку', () => {
    expect(getJoinedArrayAsString([null as unknown as string, 'b'])).toBe(';b');
  });
});
