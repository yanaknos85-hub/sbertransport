import { useEffect, useState } from 'react';

import moment, { Moment } from 'moment';

import { ScheduleFilters } from 'api/schedule2.0/schedule.types';

import { useShiftsQuery } from '../../../context/shiftsQuery.context';

const getTimes = (query: ScheduleFilters) => {
  const startDate = moment.utc(query.startDate);
  const endDate = moment.utc(query.endDate);

  const result: Moment[] = [];

  while (startDate.isBefore(endDate)) {
    result.push(startDate.clone());
    startDate.add(1, 'h');
  }

  return result;
};

const useTimes = () => {
  const { query } = useShiftsQuery();

  const [times, setTimes] = useState<Moment[]>(getTimes(query));

  useEffect(() => {
    setTimes(getTimes(query));
  }, [query]);

  return times;
};

export default useTimes;
