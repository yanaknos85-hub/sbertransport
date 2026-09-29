import { APIQueryResult, TypeAtKey, useAPI } from 'api';
import { MOCKED_API_PREFIX, GET_ALL_CARGO_CATEGORY_NAMES } from 'constants/constants.api';
import { CargoCategoryName, CargoCategoryNames } from 'stores/CargoCategoryName/CargoCategoryName.interface';

declare module 'api' {
  interface Cache {
    cargoCategoryNames: {
      key: ['cargoCategoryNames'];
      value: {
        categoryNames: CargoCategoryName[];
      };
    };
  }
}

type CargoCategoryNamesCacheItem = TypeAtKey<['cargoCategoryNames']>;

const raw2cache = (categoryNames: CargoCategoryName[]): CargoCategoryNamesCacheItem => ({
  categoryNames,
});

export const useCargoCategoryNames = (): APIQueryResult<CargoCategoryNamesCacheItem> => (
  useAPI(['cargoCategoryNames'], ({ http, process }) => http
    .get<CargoCategoryName[]>(`${MOCKED_API_PREFIX}${GET_ALL_CARGO_CATEGORY_NAMES}`)
    .then(process.decodeResponseData(CargoCategoryNames))
    .then(raw2cache)
  ));
