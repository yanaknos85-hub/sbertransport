import { ignore } from './utils';

/**
 * Получает сохраненные значения фильтров из localStorage
 * @param filtersKey - ключ сохранения фильтров
 * @returns объект со значениями фильтров или пустой объект при ошибке
 */
export const getFiltersValues = (filtersKey: string) => {
  try {
    return JSON.parse(localStorage.getItem(filtersKey) || '{}');
  } catch {
    return {};
  }
};

/**
 * Сохраняет значения фильтров в localStorage (объединяет с существующими)
 * @param filtersKey - ключ сохранения фильтров
 * @param filters - объект с фильтрами для сохранения
 */
export const saveFiltersValues = (filtersKey: string, filters = {}) => {
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
