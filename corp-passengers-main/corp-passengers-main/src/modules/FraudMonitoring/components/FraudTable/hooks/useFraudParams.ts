import { useState } from 'react';

import { filterFalsyValues, getSelectedFiltersCount, prepareFiltersForRequest } from '../utils';

import { useFraudPagination } from './useFraudPagination';

import { FraudFilters } from '../types';

export const useFraudParams = () => {
  const [filters, setFilters] = useState<FraudFilters>({});
  const {
    pagination, setPagination, resetPage,
  } = useFraudPagination();

  const set = (payload: FraudFilters) => {
    const mergeFilters = { ...filters, ...payload };
    const nonEmptyFilters = filterFalsyValues(mergeFilters);
    resetPage();

    setFilters(nonEmptyFilters);
  };

  const reset = () => setFilters({});

  const resetFilters = () => setFilters(({ humanReadableId }) => ({ humanReadableId }));

  const resetSearch = () => setFilters(prev => {
    delete prev.humanReadableId;

    return { ...prev };
  });

  const selectedFiltersCount = getSelectedFiltersCount(filters);
  const requestFilters = prepareFiltersForRequest(filters);

  return {
    filters,
    pagination,
    filtersCount: selectedFiltersCount,
    requestFilters,
    setPagination,
    set,
    reset,
    resetFilters,
    resetSearch,
  };
};
