import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { CONTRACTOR_DRIVER } from 'api/drivers/drivers.constants';

import { UseGetDriverProps, UseGetDriverResponse } from './drivers.types';

declare module 'api' {
  interface Cache {
    driver: { key: ['driver', UseGetDriverProps]; value: UseGetDriverResponse };
  }
}

export const useDriver = (
  { contractorId, driverId }: UseGetDriverProps,
  config?: QueryConfig<UseGetDriverResponse, Error>
): APIQueryResult<UseGetDriverResponse, Error> => useAPI(
  ['driver', { contractorId, driverId }],
  ({ http, process }) => http
    .get<UseGetDriverResponse>(CONTRACTOR_DRIVER, { urlParams: { contractorId, driverId } })
    .then(process.decodeResponseData(UseGetDriverResponse)),
  config
);
