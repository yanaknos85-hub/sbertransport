import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { useCallback, useEffect, useState } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { setIntoUrl } from 'utils/setIntoUrl';
import useUrl from './useUrl';

type Query<T> = T & PaginationParams;

interface QueryReturnType<T> {
  query: Query<T>;
  setQuery: (query: Omit<T, keyof PaginationParams>) => void;
  setPagination: (pagination: PaginationParams) => void;
}

export const defaultPagination: PaginationParams = { page: 0, size: 10 };

/***
 *
 * @param defaultValues - дефолтные значения фильтров (пагинация опционально)
 */
export const useQuery = <T>(
  defaultValues?: PartialBy<Query<T>, keyof PaginationParams>,
  useUrlFilters = true
): QueryReturnType<T> => {
  const { replace } = useHistory();
  const { pathname } = useLocation();

  const { testMode, ...filters } = useUrl();
  const [query, setQuery] = useState<Query<T>>({
    ...defaultPagination,
    ...defaultValues,
    ...(useUrlFilters && { ...filters }),
  } as Query<T>);

  const handleQueryChange = useCallback(newQuery => {
    setQuery(prev => ({
      ...newQuery,
      ...defaultPagination,
      size: prev.size,
    }));
  }, []);

  const handlePaginationChange = useCallback(pagination => {
    setQuery(prevQuery => ({
      ...prevQuery,
      ...pagination,
    }));
  }, []);

  useEffect(() => {
    if (!useUrlFilters) return;

    const params = new URLSearchParams();

    for (const filter in query) {
      setIntoUrl(params, query, filter);
    }

    replace({
      pathname,
      search: params.toString(),
    });
  }, [useUrlFilters, query, pathname, replace]);

  // TODO: добавить возврат к дефолтным фильтрам при сбросе в урле, как в дисп

  return {
    query,
    setQuery: handleQueryChange,
    setPagination: handlePaginationChange,
  };
};
