/**
 * Добавляет значение поля объекта в URL-параметры (поддерживает массивы)
 * @param urlParams - URLSearchParams для добавления параметров
 * @param obj - объект с данными
 * @param key - ключ поля объекта для добавления
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const setIntoUrl = (urlParams: URLSearchParams, obj: Record<string, any>, key: string) => {
  if (Array.isArray(obj[key])) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    obj[key].forEach((val: any) => {
      urlParams.append(`${key}[]`, String(val));
    });
  } else if (typeof obj[key] === 'object' && obj[key] !== null) {
    urlParams.append(key, JSON.stringify(obj[key]));
  } else if (obj[key] || obj[key] === 0 || obj[key] === false) {
    urlParams.append(key, String(obj[key]));
  }
};
