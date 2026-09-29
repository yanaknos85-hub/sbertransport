import { reject } from 'ramda';
import moment, { Moment } from 'moment';
import { UUID } from 'utils/io-ts';

export const processSearchContractsQuery = <T>(searchQuery: T & {
  period?: (Moment | string | null)[];
  organizationIds?: UUID;
}) => {
  const period
    = searchQuery.period && typeof searchQuery.period[0] === 'string'
      ? searchQuery.period.filter(date => date !== 'null')
      : searchQuery.period;

  const startDate = period?.[0];
  const endDate = period?.[1] ?? period?.[0];

  const requestBody = {
    ...searchQuery,
    organizationId: searchQuery.organizationIds,
    organizationIds: undefined,
    startDate: startDate && moment(startDate).startOf('day'),
    endDate: endDate && moment(endDate).endOf('day'),
  };

  delete requestBody.period;

  return reject(x => typeof x === 'undefined' || x === '')(requestBody);
};
