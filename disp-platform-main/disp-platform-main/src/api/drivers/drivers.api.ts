import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';
import * as t from 'io-ts';

import {
  APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import {
  CONTRACTOR_ALL_DRIVERS,
  CONTRACTOR_DRIVER,
  DRIVERS_IMPORT,
  DRIVERS_IMPORT_RESULT
} from 'api/drivers/drivers.constants';

import { DRIVER_LICENSES } from 'constants/driver.constants';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import {
  Driver,
  DriverData,
  ImportedDrivers,
  SearchDriversResponse,
  UseCreateDriverResponse,
  UseGetDriverLicensesResponse,
  UseGetDriverProps,
  UseGetDriverResponse,
  UseSearchDriversProps,
  UseUpdateDriverProps,
  UseUpdateDriverResponse
} from './drivers.types';

declare module 'api' {
  interface Cache {
    driver: { key: ['driver', UseGetDriverProps]; value: UseGetDriverResponse };
    searchDriver: { key: ['search-driver', UseSearchDriversProps]; value: SearchDriversResponse };
    driverLicenses: { key: ['driver-licenses']; value: UseGetDriverLicensesResponse };
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

export const useUpdateDriver = (
  contractorId: string
): MutationResultPair<UseUpdateDriverResponse, AxiosError, UseUpdateDriverProps, unknown> => useAPIMutation(
  ({ http, process }, { driverId, data }: UseUpdateDriverProps) => http
    .put<UseUpdateDriverResponse>(CONTRACTOR_DRIVER, data, { urlParams: { contractorId, driverId } })
    .then(process.decodeResponseData(UseUpdateDriverResponse)),
  {
    onSuccess: ({
      cache, result: _, variables: { driverId, data }, process, t,
    }) => {
      process.processStatus(200, t.Drivers.successUpdateDriver);
      updateQueryCache(cache, ['driver', { contractorId, driverId }], driver => ({ ...driver, ...data }));
      cache.refetchQueries(['search-driver']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error.response?.status === 500) {
        return logger.toMessage('error', t.Drivers.duplicateError);
      }

      if (error.response?.status !== 409) {
        logger.toMessage('error', t.Drivers.errorUpdateDriver);
      }
    },
  }
);

export const useCreateDriver = (
  contractorId: string
): MutationResultPair<UseCreateDriverResponse, AxiosError, DriverData, unknown> => useAPIMutation(
  ({ http, process }, data) => http
    .post<UseCreateDriverResponse>(CONTRACTOR_ALL_DRIVERS, data, {
      urlParams: { contractorId },
    })
    .then(process.decodeResponseData(UseCreateDriverResponse)),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Drivers.successCreateDriver);
      cache.refetchQueries(['search-driver']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error.response?.status === 500) {
        return logger.toMessage('error', t.Drivers.duplicateError);
      }

      if (error.response?.status !== 409) {
        logger.toMessage('error', t.Drivers.errorCreateDriver);
      }
    },
  }
);

export const useSearchDrivers = ({
  contractorId,
  autoparkId,
  data,
}: UseSearchDriversProps): APIQueryResult<SearchDriversResponse, Error> => (
  useAPI(
    ['search-driver', {
      contractorId, autoparkId, data,
    } as UseSearchDriversProps],
    ({ http, process }) => (
      http
        .get<SearchDriversResponse>(CONTRACTOR_ALL_DRIVERS, {
          params: {
            autoparkId,
            ...data,
          },
          urlParams: { contractorId },
        })
        .then(process.decodeResponseData(SearchDriversResponse))
        .catch(() => ({} as SearchDriversResponse))
    ),
    {
      keepPreviousData: true,
    }
  )
);

export const useSearchDriversMutation = (
  contractorId: UUID
): MutationResultPair<
  SearchDriversResponse,
  AxiosError,
  { page?: number; size?: number; driverFullName?: string; personnelNumber?: string },
  unknown
> => useAPIMutation(
  ({ http, process }, data) => http
    .get<SearchDriversResponse>(CONTRACTOR_ALL_DRIVERS, {
      urlParams: { contractorId },
      params: { ...data, isActive: true },
    })
    .then(process.decodeResponseData(SearchDriversResponse)),
  { onSuccess: async () => ({} as SearchDriversResponse) }
);

export const useGetDriverLicenses = () => Object.values(DRIVER_LICENSES).map(license => ({ name: license }));

export const useImportDrivers = (): MutationResultPair<ImportedDrivers, AxiosError, { file: File }, unknown> => (
  useAPIMutation(
    ({ http, process }, { file }) => {
      const formData = new FormData();
      formData.append('file', file);
      return http.post<ImportedDrivers>(DRIVERS_IMPORT, formData).then(process.decodeResponseData(ImportedDrivers));
    },
    { onSuccess: () => ignore() }
  )
);

export const useImportReportDrivers = (
  contractorId: UUID,
  driversProps: UseSearchDriversProps
): MutationResultPair<Driver[], AxiosError, ImportedDrivers, unknown> => useAPIMutation(
  ({ http, process }, importedDrivers) => (
    http.post<Driver[]>(DRIVERS_IMPORT_RESULT, importedDrivers)
      .then(process.decodeResponseData(t.array(Driver))
      )),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Drivers.successImport);
      cache.refetchQueries(['search-driver', { contractorId, data: driversProps }]);
    },
  }
);
