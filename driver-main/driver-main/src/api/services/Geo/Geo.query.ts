import { useQuery } from '@tanstack/react-query';
import { BaseQueryOptions } from 'api/query';
import { Route, RouteData } from './Geo.types';
import { useGeoService } from './Geo';

export enum GeoKeys {
  Route = 'route',
}

declare module 'api' {
  interface Cache {
    route: {
      key: [typeof GeoKeys.Route, RouteData];
      value: unknown;
    };
  }
}

/** Получение маршрута по координатам */
export const useRoute = (data: RouteData, config?: BaseQueryOptions<Route>) => {
  const { getRoute } = useGeoService();

  return useQuery({
    queryKey: [GeoKeys.Route, data],
    queryFn: () => getRoute(data),
    retry: 0,
    staleTime: Infinity,
    cacheTime: Infinity,
    ...config,
  });
};
