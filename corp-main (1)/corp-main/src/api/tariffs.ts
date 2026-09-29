import { APIQueryResult, useAPI } from 'api';
import * as t from 'io-ts';
import { QueryConfig } from 'react-query';
import { RegionInfoType } from 'stores/Employee/Employee.interface';

declare module 'api' {
  interface Cache {
    RegionInfoType: { key: ['RegionInfoType']; value: RegionInfoType[] };
  }
}

interface Error {
  request: {
    response: string;
    status?: number;
  };
}

export const useGetListRegions = (
  config?: QueryConfig<RegionInfoType[], Error>
): APIQueryResult<RegionInfoType[], Error> => useAPI(
  ['RegionInfoType'],
  ({ http, process: { decodeResponseData } }) => http.get<RegionInfoType[]>(`/geo-zones/`).then(decodeResponseData(t.array(RegionInfoType))),
  config
);
