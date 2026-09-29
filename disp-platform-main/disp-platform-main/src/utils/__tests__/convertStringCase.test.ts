import {
  convertCamelToSnakeCase,
  convertSnakeToCamelCase
} from '../convertStringCase';

describe('convertCamelToSnakeCase', () => {
  test('should convert simple camelCase to SNAKE_CASE', () => {
    expect(convertCamelToSnakeCase('helloWorld')).toBe('HELLO_WORLD');
    expect(convertCamelToSnakeCase('camelCase')).toBe('CAMEL_CASE');
    expect(convertCamelToSnakeCase('testString')).toBe('TEST_STRING');
  });

  test('should convert camelCase to snake_case with lowercase', () => {
    expect(convertCamelToSnakeCase('helloWorld', false)).toBe('hello_World');
    expect(convertCamelToSnakeCase('camelCase', false)).toBe('camel_Case');
    expect(convertCamelToSnakeCase('testString', false)).toBe('test_String');
  });

  test('should convert camelCase with custom splitter', () => {
    expect(convertCamelToSnakeCase('helloWorld', true, '-')).toBe('HELLO-WORLD');
    expect(convertCamelToSnakeCase('helloWorld', false, '-')).toBe('hello-World');
    expect(convertCamelToSnakeCase('helloWorld', true, '.')).toBe('HELLO.WORLD');
  });

  test('should handle strings with multiple uppercase letters', () => {
    expect(convertCamelToSnakeCase('XMLHttpRequest')).toBe('X_M_L_HTTP_REQUEST');
    expect(convertCamelToSnakeCase('IOStream')).toBe('I_O_STREAM');
    expect(convertCamelToSnakeCase('ABCDef')).toBe('A_B_C_DEF');
  });

  test('should handle strings with numbers', () => {
    expect(convertCamelToSnakeCase('version2')).toBe('VERSION2');
    expect(convertCamelToSnakeCase('test123ABC')).toBe('TEST123_A_B_C');
    expect(convertCamelToSnakeCase('field1Name')).toBe('FIELD1_NAME');
  });

  test('should handle single word', () => {
    expect(convertCamelToSnakeCase('hello')).toBe('HELLO');
    expect(convertCamelToSnakeCase('world')).toBe('WORLD');
    expect(convertCamelToSnakeCase('test', false)).toBe('test');
  });

  test('should handle empty string', () => {
    expect(convertCamelToSnakeCase('')).toBe('');
  });

  test('should handle string with only uppercase letters', () => {
    expect(convertCamelToSnakeCase('ABC')).toBe('A_B_C');
    expect(convertCamelToSnakeCase('ABC', false)).toBe('A_B_C');
  });

  test('should handle mixed case with numbers', () => {
    expect(convertCamelToSnakeCase('field1Name2')).toBe('FIELD1_NAME2');
    expect(convertCamelToSnakeCase('test123abc')).toBe('TEST123ABC');
  });
});

describe('convertSnakeToCamelCase', () => {
  test('should convert simple snake_case to camelCase', () => {
    expect(convertSnakeToCamelCase('hello_world')).toBe('helloWorld');
    expect(convertSnakeToCamelCase('camel_case')).toBe('camelCase');
    expect(convertSnakeToCamelCase('test_string')).toBe('testString');
  });

  test('should convert with hyphens', () => {
    expect(convertSnakeToCamelCase('hello-world')).toBe('helloWorld');
    expect(convertSnakeToCamelCase('my-variable')).toBe('myVariable');
  });

  test('should handle strings with multiple underscores', () => {
    expect(convertSnakeToCamelCase('a_b_c_d')).toBe('aBCD');
    expect(convertSnakeToCamelCase('hello_world_test_string')).toBe('helloWorldTestString');
  });

  test('should handle strings with numbers', () => {
    expect(convertSnakeToCamelCase('field_1_name')).toBe('field_1Name');
    expect(convertSnakeToCamelCase('test_123_var')).toBe('test_123Var');
  });

  test('should handle single word without separators', () => {
    expect(convertSnakeToCamelCase('hello')).toBe('hello');
    expect(convertSnakeToCamelCase('world')).toBe('world');
    expect(convertSnakeToCamelCase('test')).toBe('test');
  });

  test('should handle empty string', () => {
    expect(convertSnakeToCamelCase('')).toBe('');
  });

  test('should handle string with only underscores', () => {
    expect(convertSnakeToCamelCase('_')).toBe('_');
    expect(convertSnakeToCamelCase('__')).toBe('__');
    expect(convertSnakeToCamelCase('_a_')).toBe('A_');
  });

  test('should handle string starting/ending with separator', () => {
    expect(convertSnakeToCamelCase('_hello_world')).toBe('HelloWorld');
    expect(convertSnakeToCamelCase('hello_world_')).toBe('helloWorld_');
    expect(convertSnakeToCamelCase('__test__')).toBe('_Test__');
  });
});
