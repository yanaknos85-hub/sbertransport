import { MutationResultPair } from 'react-query';
import {
  updateQueryCache, APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import {
  MOCKED_API_PREFIX,
  DELETE_CARGO_TYPE,
  CREATE_CARGO_TYPE,
  UPDATE_CARGO_TYPE,
  GET_ALL_CARGO_TYPES
} from 'constants/constants.api';
import { UUID } from 'utils/io-ts';
import indexById from 'utils/indexById';
import { ignore } from 'utils';
import { CargoType, CargoTypeArray } from 'stores/CargoType/CargoType.interface';

declare module 'api' {
  interface Cache {
    cargoTypes: {
      key: ['cargoTypes'];
      value: {
        cargoTypes: CargoType[];
        byId: Record<string, CargoType | undefined>;
      };
    };
  }
}

type CacheItem = TypeAtKey<['cargoTypes']>;

const raw2cache = (cargoTypes: CargoType[]) => ({
  cargoTypes,
  byId: indexById(cargoTypes),
});

export const useCargoTypes = (organizationId: string): APIQueryResult<CacheItem, unknown> => (
  useAPI(['cargoTypes'], ({ http, process }) => http
    .get<CargoType[]>(`${MOCKED_API_PREFIX}${GET_ALL_CARGO_TYPES}`, { urlParams: { organizationId } })
    .then(process.decodeResponseData(CargoTypeArray))
    .then(raw2cache)
  ));

export const useCreateCargoType = (organizationId: string): MutationResultPair<CargoType, unknown, Omit<CargoType, 'id'>, unknown> => useAPIMutation(
  ({ http, process }, cargoType: Omit<CargoType, 'id'>) => (
    // @ts-ignore
    http.post(`${MOCKED_API_PREFIX}${CREATE_CARGO_TYPE}`, { ...cargoType }, { urlParams: { organizationId } })
    // @ts-ignore
      .then<CargoType>(process.getResponseData)
  ), {
    onSuccess: ({
      cache, result: cargoType, process, t,
    }) => {
      process.processStatus(200, t.CargoTypes.AddSuccess);
      updateQueryCache(cache, ['cargoTypes'], ({ cargoTypes }) => raw2cache([...cargoTypes, cargoType]));
    },
  }
);

export const useUpdateCargoType = (orgId: string): MutationResultPair<CargoType, unknown, CargoType, unknown> => (
  useAPIMutation(({ http, process }, cargoType: CargoType) => (http
    // @ts-ignore
    .post(`${MOCKED_API_PREFIX}${UPDATE_CARGO_TYPE}`, { ...cargoType }, { urlParams: { cargoTypeId: cargoType.id, organizationId: orgId } })
    // @ts-ignore
    .then<CargoType>(process.getResponseData)),
  {
    onSuccess: ({
      cache, variables: cargoType, process, t,
    }) => {
      process.processStatus(200, t.CargoTypes.EditSuccess);
      updateQueryCache(cache, ['cargoTypes'], ({ cargoTypes }) => (
        raw2cache(cargoTypes.map(p => (p.id === cargoType.id ? cargoType : p)))
      ));
      cache.refetchQueries(['cargoTypes']);
    },
  }
  ));

// eslint-disable-next-line @stylistic/max-len
export const useDeleteCargoType = (): MutationResultPair<void, unknown, { cargoTypeId: string | UUID; organizationId: string }, unknown> => (
  useAPIMutation(
    ({ http }, { cargoTypeId, organizationId }: { cargoTypeId: string | UUID; organizationId: string }) => http
      .delete<number>(`${MOCKED_API_PREFIX}${DELETE_CARGO_TYPE}`, { urlParams: { cargoTypeId, organizationId } })
      .then(ignore),
    {
      onSuccess: ({
        cache, variables: { cargoTypeId }, process, t,
      }) => {
        process.processStatus(200, t.CargoTypes.DeleteSuccess);
        updateQueryCache(cache, ['cargoTypes'], ({ cargoTypes }) => (
          raw2cache(cargoTypes.filter(cargoType => cargoType.id !== cargoTypeId)))
        );
        cache.refetchQueries(['cargoTypes']);
      },
    }
  ));
