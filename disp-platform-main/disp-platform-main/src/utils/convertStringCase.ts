/**
 * Преобразует строку из camelCase в snake_case
 * @param string - входная строка в camelCase
 * @param uppercase - флаг перевода в верхний регистр (по умолчанию true)
 * @param splitter - разделитель между словами (по умолчанию '_')
 * @returns строка в snake_case
 */
export const convertCamelToSnakeCase = (
  string: string,
  uppercase = true,
  splitter = '_'
): string => {
  const snakeCaseString = string.split(/(?=[A-Z])/).join(splitter);

  return uppercase ? snakeCaseString.toUpperCase() : snakeCaseString;
};

/**
 * Преобразует строку из snake_case в camelCase
 * @param string - входная строка в snake_case
 * @returns строка в camelCase
 */
export const convertSnakeToCamelCase = (string: string): string => (
  string.toLowerCase().replace(/([-_][a-z])/g, group => (
    group
      .toUpperCase()
      .replace('-', '')
      .replace('_', '')
  ))
);
