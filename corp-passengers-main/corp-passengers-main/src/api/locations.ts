import * as t from 'io-ts';
import * as R from 'ramda';
import {
  updateQueryCache, useAPI, useAPIMutation, APIQueryResult, TypeAtKey
} from 'api';
import {
  GET_ALL_LOCATIONS, ADD_LOCATION, UPDATE_LOCATION, DELETE_LOCATION
} from 'constants/constants.api';
import { UUID } from 'utils/io-ts';
import { MutationResultPair } from 'react-query';
import indexById from 'utils/indexById';
import { Location } from 'stores/Locations/Locations.interface';
import { mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    locations: {
      key: ['locations'];
      value: { locations: Location[]; byId: Record<string, Location> };
    };
  }
}

type CacheItem = TypeAtKey<['locations']>;

const raw2cache = (locations: Location[]): CacheItem => ({
  locations,
  byId: indexById(locations),
});

export const useLocations = (): APIQueryResult<CacheItem, unknown> => useAPI(['locations'], ({ http, process }) => http
  .get<Location[]>(GET_ALL_LOCATIONS)
  .then(process.decodeResponseData(t.array(Location)))
  .then(raw2cache)
);

export const useCreateLocation = (): MutationResultPair<
  Location,
  unknown,
  { location: Omit<Location, 'id' | 'fullAddress' | 'usages'> },
  unknown
> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, { location }) => http.post(ADD_LOCATION, location).then<Location>(process.getResponseData),
  {
    onSuccess: ({
      cache, result: location, process, t,
    }) => {
      process.processStatus(200, t.Locations.AddSuccess);
      updateQueryCache(cache, ['locations'], ({ locations }) => raw2cache([...locations, location]));
    },
  }
);

export const useUpdateLocation = () => useAPIMutation(
  ({ http }, { location }: { location: Omit<Location, 'fullAddress' | 'usages'> }) => http.put(UPDATE_LOCATION, location, { urlParams: { locId: location.id } }),
  {
    onSuccess: ({
      cache, variables: { location }, process, t,
    }) => {
      process.processStatus(200, t.Locations.EditSuccess);
      updateQueryCache(cache, ['locations'], ({ locations }) => raw2cache(locations.map(p => (p.id === location.id ? location : p)))
      );
    },
  }
);

export const useDeleteLocation = () => useAPIMutation(
  ({ http }, { locId }: { locId: UUID }) => http.delete<number>(DELETE_LOCATION, { urlParams: { locId } }),
  {
    onSuccess: ({
      cache, variables: { locId }, process, t,
    }) => {
      process.processStatus(200, t.Locations.DeleteSuccess);
      updateQueryCache(
        cache,
        ['locations'],
        R.pipe(R.prop('locations'), R.reject(R.whereEq({ id: locId })), raw2cache)
      );
    },
  }
);

export const useUploadLocations = mkUseUploadEntity('meetingAddress', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['locations']),
});
