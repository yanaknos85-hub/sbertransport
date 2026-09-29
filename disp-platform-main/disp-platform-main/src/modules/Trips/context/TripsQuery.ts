import { useEffect } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useQuery } from 'hooks/useQuery';
import { useFilters } from '../hooks/useFilters';
import { useTripsSettings } from './TripsSettings.context';
import { TripsFilters } from 'api/trips/trips.types';
import { PaginationParams, SortParams } from 'utils/io-ts/pagination';
import { Columns } from '../constants';
import { convertCamelToSnakeCase } from 'utils/convertStringCase';

const PASS_FILTERS = 'PASS_FILTERS';

const getSort = ({ field, direction }: SortParams): SortParams => {
  if (!field) {
    return {};
  }

  // Название поля изменилось. В фильтрах у пользователей сохранено старое.
  // Чтобы бесшовно заменить, делаем проверку
  if (field === 'START_TIME') {
    return { field: convertCamelToSnakeCase(Columns.StartTime), direction };
  }

  // Проверяем, что в field сохранено известное значение Избегаем 400 ошибки
  if (Object.values(Columns).some(value => field && convertCamelToSnakeCase(value) === field)) {
    return { field, direction };
  }

  return { field: undefined, direction: undefined };
};

const useHook = () => {
  const { defaultValues } = useFilters();

  const { settings, setTableSize } = useTripsSettings();

  const {
    query, setQuery, setSort, setPagination,
  } = useQuery<Omit<TripsFilters & SortParams, keyof PaginationParams>>(
    {
      ...defaultValues,
      ...JSON.parse(localStorage.getItem(PASS_FILTERS) || '{}'),
      ...getSort(JSON.parse(localStorage.getItem(PASS_FILTERS) || '{}')),
      size: settings.table.size,
    },
    {
      getSort,
    }
  );

  useEffect(() => {
    const { size, ...filters } = query;
    setTableSize(size);
    localStorage.setItem(PASS_FILTERS, JSON.stringify(filters));
  }, [query]);

  return {
    query,
    setQuery,
    setSort,
    setPagination,
  };
};

export const [useTripsQuery, TripsQueryProvider] = createCallableCtx(useHook, { name: 'TripsQueryProvider' });
