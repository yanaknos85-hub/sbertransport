import { ignore } from './utils';

/**
 * Получает сохраненные значения фильтров из localStorage
 * @param filtersKey - ключ для хранения фильтров в localStorage
 * @returns объект со значениями фильтров или пустой объект в случае ошибки
 */
export const getFiltersValues = (filtersKey: string): Record<string, unknown> => {
  try {
    return JSON.parse(localStorage.getItem(filtersKey) || '{}');
  } catch {
    return {};
  }
};

/**
 * Сохраняет значения фильтров в localStorage
 * @param filtersKey - ключ для хранения фильтров в localStorage
 * @param filters - объект с фильтрами для сохранения
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const saveFiltersValues = (filtersKey: string, filters: Record<any, any> = {}) => {
  try {
    localStorage.setItem(
      filtersKey,
      JSON.stringify({
        ...getFiltersValues(filtersKey),
        ...filters,
      })
    );
  } catch {
    ignore();
  }
};
