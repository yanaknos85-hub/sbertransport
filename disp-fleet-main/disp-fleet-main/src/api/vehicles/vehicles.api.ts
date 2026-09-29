import { MutationResultPair } from 'react-query';
import { AxiosError } from 'axios';

import { APIQueryResult, useAPI, useAPIMutation } from 'api/';
import { IMPORT_REPORT_KEY, mkUseUploadEntity } from 'api/upload/upload.api';
import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import indexById from 'utils/indexById';

import { VehiclesSearch } from 'types/vehicles';
import {
  CacheAllSearchedVehicle,
  CacheAllSearchedVehicles,
  EcoClass,
  Vehicle,
  VehicleSearchParams,
  VehiclesSearchResponse
} from './vehicles.types';
import {
  CREATE_VEHICLE,
  DELETE_VEHICLE,
  GET_ALL_VEHICLES_BY_CONTRACTOR,
  GET_VEHICLE_BY_AUTOPARK,
  UPDATE_VEHICLE
} from './vehicles.constants';

declare module 'api' {
  interface Cache {
    allVehiclesSearch: { key: ['allVehiclesSearch', VehiclesSearch]; value: CacheAllSearchedVehicles };
    vehicleCard: { key: ['vehicleCard', UUID, UUID, UUID]; value: Vehicle };
    ecoClasses: { key: ['eco-class']; value: EcoClass[] };
  }
}

const allSearchedVehicles2cache = (vehiclesSearchResponse: VehiclesSearchResponse): CacheAllSearchedVehicle => ({
  response: vehiclesSearchResponse,
  byId: indexById(vehiclesSearchResponse.content),
});

export const useSearchAllVehicles = ({
  contractorId,
  data,
}: VehiclesSearch): APIQueryResult<CacheAllSearchedVehicle, Error> => (
  useAPI(
    ['allVehiclesSearch', { contractorId, data }],
    ({ http, process }) => http
      .get<VehiclesSearchResponse>(GET_ALL_VEHICLES_BY_CONTRACTOR, {
        params: data,
        urlParams: { contractorId },
      })
      .then(process.decodeResponseData(VehiclesSearchResponse))
      .then(allSearchedVehicles2cache),
    {
      enabled: !!data.size,
      keepPreviousData: true,
    }
  )
);

export const useSearchAllVehiclesMutation = (
  contractorId: UUID
): MutationResultPair<VehiclesSearchResponse, AxiosError, VehicleSearchParams, unknown> => (
  useAPIMutation(
    ({ http, process }, query) => (
      http
        .get<VehiclesSearchResponse>(GET_ALL_VEHICLES_BY_CONTRACTOR, {
          params: query,
          urlParams: { contractorId },
        })
        .then(process.decodeResponseData(VehiclesSearchResponse))
    ),
    { onSuccess: async () => ({} as VehiclesSearchResponse) }
  )
);

export const useCreateVehicle = (contractorId: UUID): MutationResultPair<Vehicle, AxiosError, Vehicle, unknown> => (
  useAPIMutation(
    ({ http, process }, { autopark, ...data }) => (
      http
        .post<Vehicle>(CREATE_VEHICLE, data, { urlParams: { contractorId, autoparkId: autopark.id } })
        .then(process.decodeResponseData(Vehicle))
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.Vehicles.successCreateVehicle);
        cache.refetchQueries(['allVehiclesSearch']);
      },
      onError: ({
        error, logger, t,
      }) => {
        logger.toMessage(
          'error',
          error.response?.status === 409 ? t.Vehicles.errorDuplicateVehicle : t.Vehicles.errorCreateVehicle
        );
      },
    }
  )
);

export const useUpdateVehicle = (
  contractorId: UUID,
  vehicleId: UUID
): MutationResultPair<void, AxiosError, Vehicle, unknown> => (
  useAPIMutation(
    ({ http }, data) => (
      http
        .put(UPDATE_VEHICLE, data, {
          urlParams: {
            contractorId, autoparkId: data.autoparkId!, vehicleId,
          },
        })
        .then(ignore)
    ),
    {
      onSuccess: ({
        cache, result: _, t, process,
      }) => {
        process.processStatus(200, t.Vehicles.successUpdateVehicle);
        cache.refetchQueries(['allVehiclesSearch']);
      },

      onError: ({
        error, logger, t,
      }) => {
        logger.toMessage(
          'error',
          error.response?.status === 409 ? t.Vehicles.errorDuplicateVehicle : t.Vehicles.errorUpdateVehicle
        );
      },
    }
  )
);

export const useDeleteVehicle = (
  contractorId: UUID,
  isVehicleMoveToOtherPark = false,
  vehiclesProps: VehiclesSearch
): MutationResultPair<void, unknown, Vehicle, unknown> => (
  useAPIMutation(
    ({ http }, { autopark, id }) => (
      http.delete(DELETE_VEHICLE, {
        urlParams: {
          contractorId, autoparkId: autopark.id, vehicleId: id,
        },
      }).then(ignore)
    ),
    {
      onSuccess: ({
        cache, result: _, process, t,
      }) => {
        !isVehicleMoveToOtherPark && process.processStatus(200, t.Vehicles.successDeleteVehicle);
        cache.refetchQueries(['allVehiclesSearch', vehiclesProps]);
      },
      onError: ({ logger, t }) => {
        isVehicleMoveToOtherPark && logger.toMessage('error', t.Vehicles.errorMoveVehicleToOtherAutoPark);
        logger.toMessage('error', t.Vehicles.errorDeleteVehicle);
      },
    }
  )
);

export const useUploadVehicles = mkUseUploadEntity('vehicles', {
  onSuccess: ({ cache }) => {
    cache.refetchQueries([IMPORT_REPORT_KEY]);
  },
});

export const useGetVehicleCard = (
  contractorId: UUID,
  autoparkId: UUID,
  vehicleId: UUID
): APIQueryResult<Vehicle, Error> => (
  useAPI(['vehicleCard', contractorId, autoparkId, vehicleId], ({ http, process }) => (
    http
      .get<Vehicle>(GET_VEHICLE_BY_AUTOPARK, {
        urlParams: {
          contractorId, autoparkId, vehicleId,
        },
      })
      .then(process.decodeResponseData(Vehicle))
  )
  )
);
