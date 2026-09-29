import { useMemo } from 'react';

import { toDateRangeISO } from 'shared/components/DateInput/utils';

import { TaxiYandexRegistryFilter } from '../types/types';

export const useDisplayPeriod = (filters: TaxiYandexRegistryFilter) => useMemo(() => {
  if (!filters.desiredDateRange) {
    return [];
  }

  const dateRangeIso = toDateRangeISO(filters.desiredDateRange);

  return dateRangeIso ? [dateRangeIso] : [];
}, [filters]);
