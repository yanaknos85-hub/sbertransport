import { useEffect } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useQuery } from 'hooks/useQuery';
import { useFilters } from '../hooks/useFilters';
import { useTripsSettings } from './TripsSettings.context';
import { PaginationParams } from 'utils/io-ts/pagination';
import { TripsFilters } from 'api/trips-cargo/trips-cargo.types';

const CARGO_FILTERS = 'CARGO_FILTERS';

const useHook = () => {
  const { defaultValues } = useFilters();

  const { settings, setTableSize } = useTripsSettings();

  const {
    query, setQuery, setSort, setPagination,
  } = useQuery<Omit<TripsFilters, keyof PaginationParams>>({
    ...defaultValues,
    ...JSON.parse(localStorage.getItem(CARGO_FILTERS) || '{}'),
    size: settings.table.size,
  });

  useEffect(() => {
    const { size, ...filters } = query;
    setTableSize(size);
    localStorage.setItem(CARGO_FILTERS, JSON.stringify(filters));
  }, [query]);

  return {
    query,
    setQuery,
    setSort,
    setPagination,
  };
};

export const [useTripsQuery, TripsQueryProvider] = createCallableCtx(useHook, { name: 'TripsQueryProvider' });
