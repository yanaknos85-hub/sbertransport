import * as t from 'io-ts';

/**
 * Создает io-ts тип на основе enum
 * @param enumName - имя типа для отладки
 * @param theEnum - объект enum
 * @returns io-ts Type для значения enum
 */
export function ioTypeFromEnum<EnumType>(enumName: string, theEnum: Record<string, string | number>): t.Type<EnumType> {
  const isEnumValue = (input: unknown): input is EnumType => Object.values<unknown>(theEnum).includes(input);

  return new t.Type<EnumType>(
    enumName,
    isEnumValue,
    (input, context) => (isEnumValue(input) ? t.success(input) : t.failure(input, context)),
    t.identity
  );
}
