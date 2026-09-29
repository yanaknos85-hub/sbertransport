import * as t from 'io-ts';

/**
 * Проверяет, является ли значение значением enum
 * @param theEnum - объект enum
 * @param input - значение для проверки
 * @returns true если значение является значением enum
 */
export function isEnumValue<T>(theEnum: Record<string, T>, input: unknown): input is T {
  return Object.values<unknown>(theEnum).includes(input);
}

/**
 * Создает io-ts тип из TypeScript enum
 * @template EnumType - тип enum
 * @param enumName - название типа для отладки
 * @param theEnum - объект enum
 * @returns io-ts тип для валидации значений enum
 */
export function ioTypeFromEnum<EnumType>(enumName: string, theEnum: Record<string, string | number>): t.Type<EnumType> {
  // noinspection JSUnusedLocalSymbols
  function validator(input: unknown): input is EnumType {
    return isEnumValue(theEnum, input);
  }

  // noinspection JSUnusedLocalSymbols
  function decoder(input: unknown, context: t.Context): t.Validation<EnumType> {
    if (validator(input)) {
      return t.success(input);
    }
    return t.failure(input, context);
  }

  return new t.Type<EnumType>(enumName, validator, decoder, t.identity);
}
