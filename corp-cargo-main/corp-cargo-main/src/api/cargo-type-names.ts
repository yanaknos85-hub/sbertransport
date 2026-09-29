import { APIQueryResult, TypeAtKey, useAPI } from 'api';
import { MOCKED_API_PREFIX, GET_ALL_CARGO_TYPE_NAMES } from 'constants/constants.api';
import { indexByName } from 'utils/indexById';
import { CargoTypeName, CargoTypeNames } from 'stores/CargoTypeName/CargoTypeName.interface';

declare module 'api' {
  interface Cache {
    cargoTypeNames: {
      key: ['cargoTypeNames'];
      value: {
        cargoTypeNames: CargoTypeName[];
        byName: Record<string, CargoTypeName>;
      };
    };
  }
}

type CargoTypeNamesCacheItem = TypeAtKey<['cargoTypeNames']>;

const raw2cache = (cargoTypeNames: CargoTypeName[]): CargoTypeNamesCacheItem => ({
  cargoTypeNames,
  byName: indexByName(cargoTypeNames),
});

export const useCargoTypeNames = (): APIQueryResult<CargoTypeNamesCacheItem, unknown> => useAPI(['cargoTypeNames'], ({ http, process }) => http
  .get<CargoTypeName[]>(`${MOCKED_API_PREFIX}${GET_ALL_CARGO_TYPE_NAMES}`)
  .then(process.decodeResponseData(CargoTypeNames))
  .then(raw2cache)
);
