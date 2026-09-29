import { MutationResultPair } from 'react-query';
import {
  updateQueryCache, APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import {
  MOCKED_API_PREFIX,
  GET_ALL_CARGO_AUTO,
  GET_CAPACITY_CARGO_AUTO,
  CREATE_CARGO_AUTO,
  UPDATE_CARGO_AUTO,
  DELETE_CARGO_AUTO,
  GET_CATEGORY_CARGO_AUTO
} from 'constants/constants.api';
import { UUID } from 'utils/io-ts';
import indexById from 'utils/indexById';
import { ignore } from 'utils';
import {
  CargoAuto,
  AutoCapacity,
  CategoryCargoAuto,
  CargoAutoArray,
  CargoAutoCapacity,
  CargoAutoCategory
} from 'stores/CargoAuto/CargoAuto.interface';

declare module 'api' {
  interface Cache {
    cargoAuto: {
      key: ['cargoAuto'];
      value: {
        cargoAuto: CargoAuto[];
        byId: Record<string, CargoAuto | undefined>;
      };
    };
    autoCapacity: {
      key: ['autoCapacity'];
      value: AutoCapacity[];
    };
    autoCargoType: {
      key: ['autoCargoType'];
      value: CategoryCargoAuto[];
    };
  }
}

type CacheItem = TypeAtKey<['cargoAuto']>;

const raw2cache = (cargoAuto: CargoAuto[]) => ({
  cargoAuto,
  byId: indexById(cargoAuto),
});

export const useCargoAuto = (): APIQueryResult<CacheItem, unknown> => useAPI(['cargoAuto'], ({ http, process }) => http
  .get<CargoAuto[]>(`${MOCKED_API_PREFIX}${GET_ALL_CARGO_AUTO}`)
  .then(process.decodeResponseData(CargoAutoArray))
  .then(raw2cache)
);

export const useCreateCargoAuto = (): MutationResultPair<CargoAuto, unknown, Omit<CargoAuto, 'id'>, unknown> => useAPIMutation(
  ({ http, process }, cargoAutoItem: Omit<CargoAuto, 'id'>) => http
    .post(`${MOCKED_API_PREFIX}${CREATE_CARGO_AUTO}`, { ...cargoAutoItem })
  // @ts-ignore
    .then<CargoAuto>(process.getResponseData),
  {
    onSuccess: ({
      cache, result: cargoAutoItem, process, t,
    }) => {
      process.processStatus(200, t.CargoAuto.AddSuccess);
      updateQueryCache(cache, ['cargoAuto'], ({ cargoAuto }) => raw2cache([...cargoAuto, cargoAutoItem]));
    },
  }
);

export const useUpdateCargoAuto = (): MutationResultPair<void, unknown, CargoAuto, unknown> => useAPIMutation(
  ({ http }, cargoAutoItem: CargoAuto) => http
    .put(
      `${MOCKED_API_PREFIX}${UPDATE_CARGO_AUTO}`,
      { ...cargoAutoItem },
      { urlParams: { autoId: cargoAutoItem.id } }
    )
    .then(ignore),
  {
    onSuccess: ({
      cache, variables: cargoAutoItem, process, t,
    }) => {
      process.processStatus(200, t.CargoAuto.EditSuccess);
      updateQueryCache(cache, ['cargoAuto'], ({ cargoAuto }) => raw2cache(cargoAuto.map(p => (p.id === cargoAutoItem.id ? cargoAutoItem : p)))
      );
    },
  }
);

export const useDeleteCargoAuto = (): MutationResultPair<void, unknown, { autoId: UUID }, unknown> => useAPIMutation(
  ({ http }, { autoId }: { autoId: UUID }) => http
    .delete<number>(`${MOCKED_API_PREFIX}${DELETE_CARGO_AUTO}`, { urlParams: { autoId } })
    .catch(err => {
      if (err.response.status === 409) {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return {} as any;
      }
    }),
  {
    onSuccess: ({
      cache, variables: { autoId }, process, t, result,
    }) => {
      if (result.status === 200) {
        process.processStatus(200, t.CargoAuto.DeleteSuccess);
        updateQueryCache(cache, ['cargoAuto'], ({ cargoAuto }) => raw2cache(cargoAuto.filter(cargoAutoItem => cargoAutoItem.id !== autoId))
        );
      }
    },
  }
);

export const useAutoCapacity = (): APIQueryResult<AutoCapacity[], Error> => (
  useAPI(['autoCapacity'], ({ http, process: { decodeResponseData } }) => http
    .get<AutoCapacity[]>(GET_CAPACITY_CARGO_AUTO)
    .then(decodeResponseData(CargoAutoCapacity)))
);

export const useAutoCargoType = (): APIQueryResult<CategoryCargoAuto[], Error> => (
  useAPI(['autoCargoType'], ({ http, process: { decodeResponseData } }) => http
    .get<CategoryCargoAuto[]>(GET_CATEGORY_CARGO_AUTO)
    .then(decodeResponseData(CargoAutoCategory)))
);
