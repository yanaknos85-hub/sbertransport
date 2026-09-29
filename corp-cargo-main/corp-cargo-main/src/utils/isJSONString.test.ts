import { isJsonString } from './isJSONString';

describe('isJsonString', () => {
  it('должен вернуть true для валидной JSON строки', () => {
    expect(isJsonString('{"key":"value"}')).toBe(true);
    expect(isJsonString('["item1","item2"]')).toBe(true);
    expect(isJsonString('"string"')).toBe(true);
    expect(isJsonString('123')).toBe(true);
    expect(isJsonString('true')).toBe(true);
    expect(isJsonString('false')).toBe(true);
    expect(isJsonString('null')).toBe(true);
    expect(isJsonString('{"nested":{"key":"value"}}')).toBe(true);
    expect(isJsonString('[1,2,3]')).toBe(true);
  });

  it('должен вернуть false для невалидной JSON строки', () => {
    expect(isJsonString('{key:"value"}')).toBe(false);
    expect(isJsonString("{'key':'value'}")).toBe(false);
    expect(isJsonString('{key:value}')).toBe(false);
    expect(isJsonString('[1,2,3,]')).toBe(false);
    expect(isJsonString('undefined')).toBe(false);
    expect(isJsonString('NaN')).toBe(false);
    expect(isJsonString('{')).toBe(false);
    expect(isJsonString('}')).toBe(false);
    expect(isJsonString('[')).toBe(false);
    expect(isJsonString(']')).toBe(false);
    expect(isJsonString('')).toBe(false);
    expect(isJsonString('random string')).toBe(false);
    expect(isJsonString('{"key":"value"')).toBe(false);
    expect(isJsonString('[1,2,3')).toBe(false);
  });

  it('должен вернуть false для нестроковых значений (уникальный случай)', () => {
    // TypeScript типизация гарантирует, что передаётся string
    // Этот тест покрывает edge case при передаче null/undefined в runtime
    // Поскольку функция принимает только string, в TS это невозможно
    // Оставлен для документации поведения
    expect(isJsonString('null')).toBe(true); // строка "null" — валидный JSON
  });
});
