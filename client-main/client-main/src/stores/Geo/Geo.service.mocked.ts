import { injectable } from 'inversify';

import { RequestRoute, TWaypoint } from 'shared/models/geo/types';

import { IGeoService } from './Geo.interface';

@injectable()
export class GeoServiceMocked implements IGeoService {
  getAddressByCoordinates = async (): Promise<TWaypoint[]> => (await import('mock/stores/Geo/getAddressByCoordinates.json')).default as TWaypoint[];

  getAddressBySortCoordinates = async (): Promise<TWaypoint[]> => (await import('mock/stores/Geo/getAddressByCoordinates.json')).default as TWaypoint[];

  getCoordinateByAddress = async (): Promise<TWaypoint[]> => (await import('mock/stores/Geo/getCoordinateByAddress.json')).default as TWaypoint[];

  calcRoute = async (): Promise<RequestRoute> => (await import('mock/stores/Geo/calcRoute.json')).default as RequestRoute;
}
