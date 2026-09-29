import { useCallback, useMemo } from 'react';
import { AutoparkFiltersKeys } from 'constants/app.constants';
import { useRoleMap } from 'hooks/useRoleMap';
import { getFiltersValues, saveFiltersValues } from 'utils/filtersStorage';

export interface AutoparkFilters {
  autoparkId?: string;
  branchId?: string;
}

/** Хук для получения и сохранения в storage фильтров автопарка по ролям */
const useAutoparkFilters = (): {
  filters: AutoparkFilters;
  saveFilters: (filters: AutoparkFilters) => void;
} => {
  const { isAdmin, isManager } = useRoleMap();

  const getFilters = useCallback(() => {
    if (isAdmin) {
      return getFiltersValues(AutoparkFiltersKeys.ADMIN_FILTERS_SETTING);
    }

    if (isManager) {
      return getFiltersValues(AutoparkFiltersKeys.MANAGER_FILTERS_SETTING);
    }

    return {};
  }, [isAdmin, isManager]);

  const saveFilters = useCallback(
    (filters: AutoparkFilters) => {
      if (isAdmin) {
        return saveFiltersValues(AutoparkFiltersKeys.ADMIN_FILTERS_SETTING, filters);
      }

      if (isManager) {
        return saveFiltersValues(AutoparkFiltersKeys.MANAGER_FILTERS_SETTING, filters);
      }
    },
    [isAdmin, isManager]
  );

  return useMemo(
    () => ({
      filters: getFilters(),
      saveFilters,
    }),
    [getFilters, saveFilters]
  );
};

export default useAutoparkFilters;
