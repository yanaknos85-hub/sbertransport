import { reject } from 'ramda';
import moment, { Moment } from 'moment';

export const processSearchContractsQuery = <T>(searchQuery: T & { date?: (Moment | string | null)[] }) => {
  const period
    = searchQuery.date && typeof searchQuery.date[0] === 'string'
      ? searchQuery.date.filter(date => date !== 'null')
      : searchQuery.date;

  const startDate = period?.[0];
  const endDate = period?.[1] ?? period?.[0];

  const requestBody = {
    ...searchQuery,
    startDate: startDate && moment(startDate),
    endDate: endDate && moment(endDate),
  };

  delete requestBody.date;

  return reject(x => typeof x === 'undefined' || x === '')(requestBody);
};
