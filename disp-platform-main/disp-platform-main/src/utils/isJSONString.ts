/**
 * Проверяет, является ли строка корректным JSON
 * @param str - строка для проверки
 * @returns true если строка является валидным JSON, false в противном случае
 */
export function isJsonString(str: string): boolean {
  try {
    JSON.parse(str);
  } catch (e) {
    return false;
  }

  return true;
}
