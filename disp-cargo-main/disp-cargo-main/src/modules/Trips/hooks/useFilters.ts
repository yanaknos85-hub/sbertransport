import { useMemo } from 'react';
import { activeTripStatuses } from 'constants/trips.constants';
import { SortOrderToDirectionMap } from 'constants/app.constants';
import { Columns } from '../constants';
import { convertCamelToSnakeCase } from 'utils/convertStringCase';

export const useFilters = () => {
  const defaultValues = useMemo(
    () => ({
      field: convertCamelToSnakeCase(Columns.StartTime),
      direction: SortOrderToDirectionMap.ascend,
      statuses: activeTripStatuses, // по умолчанию включены все активные
    }),
    []
  );

  return { defaultValues };
};
