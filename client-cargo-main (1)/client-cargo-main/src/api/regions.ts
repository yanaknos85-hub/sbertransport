import * as t from 'io-ts';

import { APIQueryResult, useAPI } from 'api';
import * as tt from 'utils/io-ts';

import { GEO_ZONES } from '../constants/constants.api';

declare module 'api' {
  interface Cache {
    RegionInfoTypeCargo: { key: ['RegionInfoTypeCargo']; value: RegionInfoType[] };
  }
}

export const RegionInfoType = t.intersection([
  t.type({
    id: t.string, name: t.string, code: t.union([t.string, t.number]),
  }),
  t.partial({
    parent_id: tt.uuid,
  }),
]);
export type RegionInfoType = t.TypeOf<typeof RegionInfoType>;

export const useGetListRegions = (): APIQueryResult<RegionInfoType[], Error> => useAPI(['RegionInfoTypeCargo'], ({ http, process: { decodeResponseData } }) => http.get<RegionInfoType[]>(`/${GEO_ZONES}/`).then(decodeResponseData(t.array(RegionInfoType)))
);
