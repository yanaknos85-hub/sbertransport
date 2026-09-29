import moment from 'moment';

import { ScheduleFilters } from 'api/schedule2.0/schedule.types';

import { useQuery } from 'hooks/useQuery';

import { createCallableCtx } from 'utils/createCallableContext';
import { PaginationParams } from 'utils/io-ts/pagination';

const SHIFTS_FILTERS = 'SHIFTS_FILTERS';

const startDate = moment()
  .startOf('day')
  .toISOString();
const endDate = moment()
  .endOf('month')
  .toISOString();

const initialQuery: Omit<ScheduleFilters, keyof PaginationParams> = {
  startDate,
  endDate,
};

const useHook = () => {
  const {
    query, setQuery, setPagination,
  } = useQuery<ScheduleFilters>(initialQuery, { localStorageName: SHIFTS_FILTERS });

  return {
    query,
    setQuery,
    setPagination,
  };
};

export const [useShiftsQuery, ShiftsQueryProvider] = createCallableCtx(useHook, { name: 'ShiftsQueryProvider' });
