import { useCallback, useEffect } from 'react';
import debounce from 'lodash.debounce';
import moment from 'moment';

import { FilterSettingsType } from 'stores/Cargos/types';

export interface UseSaveFiltersReturn {
  setFilterState: (values: FilterSettingsType | Partial<Record<string, any>>) => void;
  getFilterState: () => Partial<Record<string, any>>;
  resetFilerState: () => void;
  filledCountStorage: () => number;
  saveCount: (values: FilterSettingsType) => void;
}

/**
 * Хук для управления фильтрами с сохранением в sessionStorage
 * Добавляет debounce для оптимизации записи и cleanup при размонтировании
 */
export const useSaveFilters = (): UseSaveFiltersReturn => {
  const STORAGE_KEY_FILTERS = 'journalFilters';
  const STORAGE_KEY_COUNT = 'count';

  // Дебаунсенная запись в sessionStorage (300ms)
  const debouncedSetFilterState = useCallback(
    debounce((values: FilterSettingsType) => {
      sessionStorage.setItem(STORAGE_KEY_FILTERS, JSON.stringify({ ...values }));
    }, 300),
    []
  );

  const debouncedSaveCount = useCallback(
    debounce((count: number) => {
      sessionStorage.setItem(STORAGE_KEY_COUNT, JSON.stringify(count));
    }, 300),
    []
  );

  const setFilterState = useCallback((values: FilterSettingsType | Partial<Record<string, any>>) => {
    // Конвертируем moment-объекты в строки перед сохранением (если есть)
    const serializedFilters = { ...values } as FilterSettingsType;
    if (serializedFilters.desiredDate && Array.isArray(serializedFilters.desiredDate)) {
      serializedFilters.desiredDate = serializedFilters.desiredDate.map(d => {
        if (moment.isMoment(d)) {
          return d.toISOString();
        }
        return d;
      });
    }
    if (serializedFilters.shipmentTime && Array.isArray(serializedFilters.shipmentTime)) {
      serializedFilters.shipmentTime = serializedFilters.shipmentTime.map(d => {
        if (moment.isMoment(d)) {
          return d.toISOString();
        }
        return d;
      });
    }

    // Сразу записываем, debounced версия для оптимизации
    sessionStorage.setItem(STORAGE_KEY_FILTERS, JSON.stringify(serializedFilters));
    debouncedSetFilterState.cancel();
    debouncedSetFilterState(serializedFilters as FilterSettingsType);
  }, [debouncedSetFilterState]);

  const getFilterState = useCallback((): Partial<Record<string, any>> => {
    const saved = sessionStorage.getItem(STORAGE_KEY_FILTERS);
    if (!saved) return {};

    const parsed = JSON.parse(saved);

    // Преобразование строк дат в moment-объекты
    if (parsed.desiredDate && Array.isArray(parsed.desiredDate)) {
      parsed.desiredDate = [
        parsed.desiredDate[0] ? moment(parsed.desiredDate[0]) : null,
        parsed.desiredDate[1] ? moment(parsed.desiredDate[1]) : null,
      ] as [moment.Moment | null, moment.Moment | null];
    }

    if (parsed.shipmentTime && Array.isArray(parsed.shipmentTime)) {
      parsed.shipmentTime = [
        parsed.shipmentTime[0] ? moment(parsed.shipmentTime[0]) : null,
        parsed.shipmentTime[1] ? moment(parsed.shipmentTime[1]) : null,
      ] as [moment.Moment | null, moment.Moment | null];
    }

    return parsed;
  }, []);

  const resetFilerState = useCallback(() => {
    sessionStorage.removeItem(STORAGE_KEY_FILTERS);
    sessionStorage.setItem(STORAGE_KEY_COUNT, JSON.stringify(0));
  }, []);

  const countFilledFieldsStorage = useCallback((values: FilterSettingsType): number => {
    return Object.entries(values).filter(([key, value]) => {
      // Игнорируем служебные поля
      if (key === 'sortSetting' || key === 'pageSetting') return false;

      if (value === undefined || value === null || value === '') return false;
      if (Array.isArray(value) && value.length === 0) return false;

      // Проверка moment-объектов (диапазоны дат)
      if (moment.isMoment(value)) return true;
      if (moment.isMoment((value as any)?.[0]) || moment.isMoment((value as any)?.[1])) {
        return true;
      }

      // Проверка объектов с данными
      if (typeof value === 'object' && value !== null) {
        return Object.values(value).some(v => v != null);
      }
      return true;
    }).length;
  }, []);

  const saveCount = useCallback((values: FilterSettingsType) => {
    const count = countFilledFieldsStorage(values);
    sessionStorage.setItem(STORAGE_KEY_COUNT, JSON.stringify(count));
    debouncedSaveCount(count);
  }, [countFilledFieldsStorage, debouncedSaveCount]);

  const filledCountStorage = useCallback((): number => {
    const saved = sessionStorage.getItem(STORAGE_KEY_FILTERS);
    const filters = saved ? (JSON.parse(saved) as FilterSettingsType) : {};
    return countFilledFieldsStorage(filters);
  }, [countFilledFieldsStorage]);

  // Cleanup при размонтировании хука
  useEffect(() => {
    return () => {
      debouncedSetFilterState.cancel();
      debouncedSaveCount.cancel();
    };
  }, [debouncedSetFilterState, debouncedSaveCount]);

  return {
    setFilterState,
    getFilterState,
    resetFilerState,
    filledCountStorage,
    saveCount,
  };
};
