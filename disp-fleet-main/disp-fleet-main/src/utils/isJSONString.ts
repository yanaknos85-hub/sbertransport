/**
 * Проверяет, является ли строка валидным JSON
 * @param str - проверяемая строка
 * @returns true если строка является JSON, false иначе
 */
export function isJsonString(str: string): boolean {
  try {
    JSON.parse(str);
  } catch (e) {
    return false;
  }

  return true;
}
