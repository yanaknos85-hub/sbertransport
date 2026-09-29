import * as t from 'io-ts';
import {
  APIQueryResult, TypeAtKey, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import { UUID } from 'utils/io-ts';
import { GEO_ZONES, GEO_ZONES_ID, GET_ALL_GEO_ZONES } from 'constants/constants.api';
import indexById from 'utils/indexById';
import { getErrorMessage, ignore } from 'utils';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';
import { mkUseUploadEntity } from '../../upload';

declare module 'api' {
  interface Cache {
    zones: {
      key: ['zones'];
      value: {
        zones: GeoZones[];
        byId: Record<string, GeoZones>;
      };
    };
    zone: { key: ['zone', UUID]; value: GeoZones };
  }
}

type ZonesCacheItem = TypeAtKey<['zones']>;

const raw2cache = (zones: GeoZones[]): ZonesCacheItem => ({
  zones,
  byId: indexById(zones),
});

export const useZones = (): APIQueryResult<ZonesCacheItem, unknown> => useAPI(['zones'], ({ http, process }) => http
  .get<GeoZones[]>(GET_ALL_GEO_ZONES)
  .then(process.decodeResponseData(t.array(GeoZones)))
  .then(raw2cache)
);

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useCreateZone = () => useAPIMutation(
  ({ http, process }, zone: Omit<GeoZones, 'id'>) => (
    // @ts-ignore
    http.post(`${GEO_ZONES}/`, zone, {}).then<GeoZones>(process.getResponseData)
  ), {
    onSuccess: ({
      cache, result: zone, process, t,
    }) => {
      process.processStatus(200, t.Geo.AddSuccess);
      updateQueryCache(cache, ['zones'], ({ zones }) => raw2cache([...zones, zone]));
    },
    onError: ({
      error, logger, t,
    }) => {
      logger.toMessage('error', `${t.Geo.InternalServerError}: ${getErrorMessage(error)}`);
    },
  }
);

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useUpdateZone = () => useAPIMutation(
  ({ http }, zone: GeoZones) => http.put(`/${GEO_ZONES_ID}/`, zone, { urlParams: { zoneId: zone.id } }).then(ignore),
  {
    onSuccess: ({
      cache, variables: geoZone, process, t,
    }) => {
      process.processStatus(200, t.Geo.EditSuccess);
      updateQueryCache(cache, ['zones'], ({ zones }) => raw2cache(zones.map(zone => (geoZone.id === zone.id ? zone : geoZone)))
      );
    },
  }
);

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useDeleteZone = () => useAPIMutation(
  ({ http }, ZoneID: UUID) => http
    .delete<number>(`/${GEO_ZONES_ID}/`, { urlParams: { zoneId: ZoneID } })
    .then(ignore),
  {
    onSuccess: ({
      cache, variables: ZoneID, process, t,
    }) => {
      process.processStatus(200, t.Geo.DeleteSuccess);
      updateQueryCache(cache, ['zones'], ({ zones }) => raw2cache(zones.filter(zone => zone.id !== ZoneID)));
    },
  }
);

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useGetOrganizationById = (zoneId: UUID) => useAPI(['zone', zoneId as UUID], ({ http, process }) => http
  .get<GeoZones>(`${GEO_ZONES_ID}/`, { urlParams: { id: zoneId } })
  .then(process.decodeResponseData(GeoZones))
);

export const useUploadZones = mkUseUploadEntity('zones', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['zones']),
});
