import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';

import { GET_FLATS } from '../constants/constants.api';
import { FurnitureItemType } from '../stores/Cargo/Cargo.interface';

declare module 'api' {
  interface Cache {
    relocation: {
      key: ['relocation', string];
      value: FurnitureItemType[];
    };
  }
}

export const useGetFlatFurniture = (
  flatType: string,
  options?: QueryConfig<FurnitureItemType[], unknown>
): APIQueryResult<FurnitureItemType[], unknown> => useAPI(
  ['relocation', flatType],
  ({ http, process }) => http.get<FurnitureItemType[]>(GET_FLATS, { urlParams: { flatType } }).then(process.getResponseData),
  options
);
