import { createCallableCtx } from 'utils/createCallableContext';
import { useQuery } from 'hooks/useQuery';
import useFilters from '../hooks/useFilters';
import { TripsReportsFilters } from 'api/trips-reports/trips-reports.types';
import { PaginationParams } from 'utils/io-ts/pagination';

const useHook = () => {
  const { defaultValues } = useFilters();

  const {
    query, setQuery, setSort, setPagination,
  } = useQuery<Omit<TripsReportsFilters, keyof PaginationParams>>(defaultValues);

  return {
    query,
    setQuery,
    setSort,
    setPagination,
  };
};

export const [useTripsTabQuery, TripsTabQueryProvider] = createCallableCtx(useHook, { name: 'TripsTabQueryProvider' });
