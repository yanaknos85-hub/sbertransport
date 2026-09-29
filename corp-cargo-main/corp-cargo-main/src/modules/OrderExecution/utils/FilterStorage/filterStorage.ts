import { ignore } from "utils";
import type { Category } from "../../constants/Tabs";
import type { IFilter } from "../../interfaces/Filters.interface";

const STORAGE_KEY = 'orderExecutionFilter';

type FilterSessionData = Partial<Record<Category, IFilter>>;

export const filterSession = {
  get data(): FilterSessionData | null {
    try {
      const storedData = sessionStorage.getItem(STORAGE_KEY);
      // Если в sessionStorage ничего нет, возвращаем null
      if (storedData === null) {
        return null;
      }
      return JSON.parse(storedData);
    } catch {
      return null;
    }
  },
  set data(filter: FilterSessionData | null) {
    try {
      // Получаем текущие данные, чтобы не перезаписать фильтры для других категорий
      const currentData = this.data || {};
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
        ...currentData,
        ...filter,
      }));
    } catch {
      ignore();
    }
  },
};
