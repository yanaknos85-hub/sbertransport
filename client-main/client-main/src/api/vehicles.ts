import { Employee } from '@sber-sbertransport/mf-core';

import * as t from 'io-ts';
import { QueryConfig, MutationResultPair } from 'react-query';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';

import { CLEAR_QUERY_CONFIG, SYSTEM_MESSAGES } from 'constants/constants.app';
import { VEHICLES, VEHICLES_DETAILED } from 'constants/constants.env';
import { IOVehicle, Vehicle } from 'modules/ProfilePage/components/Vehicles/types/vehicles.types';

declare module 'api' {
  interface Cache {
    vehicles: {
      key: ['vehicles'];
      value: Vehicle[];
    };
    vehicle: {
      key: ['vehicle', string];
      value: Vehicle;
    };
  }
}

export const defaultOptions = {
  ...CLEAR_QUERY_CONFIG,
  enabled: true,
};

export const useVehicles = (
  {
    organizationId: orgId, departmentId: depId, id: empId,
  }: Employee,
  options?: QueryConfig<Vehicle[], unknown>
): APIQueryResult<Vehicle[]> => useAPI(
  ['vehicles'],
  ({ http, process }) => http
    .get<Vehicle[]>(VEHICLES, {
      urlParams: {
        orgId, depId, empId,
      },
    })
    .then(process.decodeResponseData(t.array(IOVehicle)))
    .catch(err => {
      process.processStatus(404, err);
      return [];
    }),
  {
    ...defaultOptions, cacheTime: 100, ...options,
  }
);

export const useVehicle = (
  {
    organizationId: orgId, departmentId: depId, id: empId,
  }: Employee,
  vehicleId: string,
  options?: QueryConfig<Vehicle, Error>
): APIQueryResult<Vehicle, Error> => useAPI(
  ['vehicle', vehicleId],
  ({ http, process }) => http
    .get<Vehicle>(VEHICLES_DETAILED, {
      urlParams: {
        orgId, depId, empId, vehicleId,
      },
    })
    .then(process.decodeResponseData(IOVehicle)),
  {
    ...defaultOptions, cacheTime: 100, suspense: true, ...options,
  }
);

export const useAddVehicle = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, Vehicle, unknown> => useAPIMutation(
  ({ http, process }, { ...data }) => http.post<Vehicle>(VEHICLES, { ...data }, {
    urlParams: {
      orgId, depId, empId,
    },
  }).then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.vechicleAddSuccess);
      cache.refetchQueries(['vehicles']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.vechicleAddFailed);
    },
  }
);

export const useEditVehicle = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, Vehicle, unknown> => useAPIMutation(
  ({ http, process }, { ...data }) => http
    .put<Vehicle>(VEHICLES_DETAILED, { ...data }, {
      urlParams: {
        orgId, depId, empId, vehicleId: data.id,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.vechicleEditSuccess);
      cache.refetchQueries(['vehicles']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.vechicleEditFailed);
    },
  }
);

export const useDeleteVehicle = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, { vehicleId: string }, unknown> => useAPIMutation(
  ({ http, process }, { vehicleId }) => http.delete(VEHICLES_DETAILED, {
    urlParams: {
      orgId, depId, empId, vehicleId,
    },
  }).then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.vechicleDeleteSuccess);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.vechicleDeleteFailed);
    },
  }
);
