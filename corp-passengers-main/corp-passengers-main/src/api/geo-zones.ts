import { useAPI } from 'api';
import { GET_ALL_GEO_ZONES } from 'constants/constants.api';
import * as t from 'io-ts';
import { QueryConfig } from 'react-query';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';

const GEO_ZONES = Symbol('geo_zones');

declare module 'api' {
  interface Cache {
    geoZones: {
      key: [typeof GEO_ZONES];
      value: GeoZones[];
    };
  }
}

export const useGeoZones = (config?: QueryConfig<GeoZones[]>) => useAPI(
  [GEO_ZONES],
  ({ http, process }) => http.get<GeoZones[]>(GET_ALL_GEO_ZONES).then(process.decodeResponseData(t.array(GeoZones))),
  config
);
