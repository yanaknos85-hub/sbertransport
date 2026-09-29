// TODO переделать в класс mobx, который сам будет доставать http

import { useAppStore } from 'stores/stores.context';
import { Route, RouteData, TWaypoint } from './Geo.types';
import { LatLngTuple } from './Geo.types';

const GEO_SERVICE = '/geo';

enum GeoEndpoints {
  Route = `${GEO_SERVICE}/route`,
  GetCoordinates = `${GEO_SERVICE}/address`,
}

export const useGeoService = () => {
  const { http, process } = useAppStore();

  const getRoute = (data: RouteData) => {
    return http
      .post<Route>(GeoEndpoints.Route, data)
      .then(process.decodeResponseData());
  };

  const getCoordinateByAddress = async (location: string): Promise<TWaypoint[]> => {
    return await http
      .get<TWaypoint[]>(GeoEndpoints.GetCoordinates, {
        params: {
          location,
          centerLatitude: '55.752692999999965',
          centerLongitude: '37.617423',
        },
      })
      .then(process.getResponseData);
  };

  const getAddressByCoordinates = async (coordinates: LatLngTuple): Promise<TWaypoint[]> => {
    const latitude = coordinates[1].toFixed(6);
    const longitude = coordinates[0].toFixed(6);

    return await http
      .get<TWaypoint[]>(GeoEndpoints.GetCoordinates, {
        params: {
          latitude, longitude, radius: 50,
        },
      })
      .then(process.getResponseData);
  };

  return {
    getRoute,
    getCoordinateByAddress,
    getAddressByCoordinates,
  };
};
