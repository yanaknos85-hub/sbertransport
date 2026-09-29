import { useMemo } from 'react';
import moment, { Moment } from 'moment';

export const useValues = <T>(query: T & { date?: (Moment | string | null)[] }) => {
  const getPeriodValue = (value: Moment | string | null) => (value ? moment(value) : null);

  const initialValues = useMemo(() => {
    const period
      = query.date && typeof query.date[0] === 'string'
        ? query.date.filter(value => value !== 'null')
        : query.date;

    return {
      ...query,
      date: period ? [getPeriodValue(period[0]), getPeriodValue(period[1])] : null,
    };
  }, [query]);

  return { initialValues };
};
