/**
 * Добавляет значение из объекта в URLSearchParams
 * @param urlParams - объект URLSearchParams
 * @param obj - объект с данными
 * @param key - ключ для извлечения значения из объекта
 * @description Если значение является массивом, добавляются параметры с ключом key[] для каждого элемента. Если значение равно null/undefined, оно игнорируется (кроме 0 и false)
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const setIntoUrl = (urlParams: URLSearchParams, obj: Record<string, any> | null | undefined, key: string) => {
  if (!obj) {
    return;
  }
  const value = obj[key];
  if (Array.isArray(value)) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    value.forEach((val: any) => {
      urlParams.append(`${key}[]`, String(val));
    });
  } else if (value || value === 0 || value === false) {
    urlParams.append(key, String(value));
  }
};
