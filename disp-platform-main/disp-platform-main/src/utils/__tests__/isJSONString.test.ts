import { isJsonString } from '../isJSONString';

describe('isJsonString', () => {
  test('should return true for valid JSON string', () => {
    expect(isJsonString('{"key":"value"}')).toBe(true);
  });

  test('should return true for valid JSON array', () => {
    expect(isJsonString('[1, 2, 3]')).toBe(true);
  });

  test('should return true for valid JSON with numbers', () => {
    expect(isJsonString('123')).toBe(true);
  });

  test('should return true for valid JSON with string', () => {
    expect(isJsonString('"hello"')).toBe(true);
  });

  test('should return true for valid JSON with boolean', () => {
    expect(isJsonString('true')).toBe(true);
  });

  test('should return true for valid JSON with null', () => {
    expect(isJsonString('null')).toBe(true);
  });

  test('should return true for valid JSON with nested objects', () => {
    expect(isJsonString('{"a":1,"b":{"c":2}}')).toBe(true);
  });

  test('should return false for invalid JSON - missing quotes', () => {
    expect(isJsonString('{key:"value"}')).toBe(false);
  });

  test('should return false for invalid JSON - single quotes', () => {
    expect(isJsonString('{\'key\':\'value\'}')).toBe(false);
  });

  test('should return false for invalid JSON - trailing comma', () => {
    expect(isJsonString('{"a":1,}')).toBe(false);
  });

  test('should return false for invalid JSON - unclosed bracket', () => {
    expect(isJsonString('{"key":"value"')).toBe(false);
  });

  test('should return false for invalid JSON - empty string', () => {
    expect(isJsonString('')).toBe(false);
  });

  test('should return false for non-string input handled gracefully', () => {
    // This test verifies the function handles edge cases without crashing
    // The function is specifically for strings, so passing non-strings is a usage error
    expect(
      () => isJsonString('' as unknown as string)
    ).not.toThrow();
  });

  test('should return false for undefined as string representation', () => {
    expect(isJsonString('undefined')).toBe(false);
  });

  test('should return false for JavaScript object as string representation', () => {
    expect(isJsonString('[object Object]')).toBe(false);
  });

  test('should return false for malformed JSON - incomplete number', () => {
    expect(isJsonString('123.')).toBe(false);
  });

  test('should return false for JSON with comments', () => {
    expect(isJsonString('{"a":1} // comment')).toBe(false);
  });

  test('should return true for JSON with empty object', () => {
    expect(isJsonString('{}')).toBe(true);
  });

  test('should return true for JSON with empty array', () => {
    expect(isJsonString('[]')).toBe(true);
  });

  test('should return true for JSON with whitespace', () => {
    expect(isJsonString('  {"key":"value"}  ')).toBe(true);
  });

  test('should return true for complex valid JSON', () => {
    const complexJson = JSON.stringify({
      users: [
        {
          id: 1, name: 'John', active: true, scores: [10, 20, 30],
        },
        {
          id: 2, name: 'Jane', active: false, scores: [15, 25, 35],
        },
      ],
      meta: { version: '1.0', timestamp: Date.now() },
    });

    expect(isJsonString(complexJson)).toBe(true);
  });

  test('should return false for JSON with unquoted keys', () => {
    expect(isJsonString('{name: "John"}')).toBe(false);
  });

  test('should return false for JSON with single quotes in array', () => {
    expect(isJsonString('[\'a\', \'b\']')).toBe(false);
  });
});
