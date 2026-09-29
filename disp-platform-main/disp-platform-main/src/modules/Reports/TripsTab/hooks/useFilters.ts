import { useMemo } from 'react';

import { TripsReportsFilters } from 'api/trips-reports/trips-reports.types';
import { SortOrderToDirectionMap } from 'constants/app.constants';
import { PaginationParams } from 'utils/io-ts/pagination';
import { convertCamelToSnakeCase } from 'utils/convertStringCase';

const useFilters = () => {
  const defaultValues: Omit<TripsReportsFilters, keyof PaginationParams> = useMemo(
    () => ({
      field: convertCamelToSnakeCase('requestHumanReadableId'),
      direction: SortOrderToDirectionMap.ascend,
    }),
    []
  );

  return { defaultValues };
};

export default useFilters;
