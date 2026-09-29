import { ActiveTripStore } from './ActiveTrip/ActiveTripStore';
import { IAuthStore } from './Auth/auth.interfaces';
import { IConfigStore } from './Config/Config.interface';
import { ResponseService } from './Http/Response.service';
import { IHttpService } from './Http/http.interface';
import { MainLayoutStore } from './MainLayout/MainLayoutStore';
import { MapStore } from './Map/MapStore';

export const TYPES = {
  IAuthService: Symbol.for('IAuthService'),
  IAuthStore: Symbol.for('IAuthStore'),
  IConfigStore: Symbol.for('IConfigStore'),
  Token: Symbol.for('Token'),
  IHttpServiceFactory: Symbol.for('IHttpServiceFactory'),
  IHttpService: Symbol.for('IHttpService'),
  IResponseService: Symbol.for('IResponseService'),
  // IHistory: Symbol.for('IHistory'),
  INavigator: Symbol.for('INavigator'),
  MapStore: Symbol.for('MapStore'),
  MainLayoutStore: Symbol.for('MainLayoutStore'),
  ActiveTripStore: Symbol.for('ActiveTripStore'),
};

export interface IRootStore {
  authStore: IAuthStore;
  configStore: IConfigStore;
  http: IHttpService;
  process: ResponseService;
  mapStore: MapStore;
  mainLayoutStore: MainLayoutStore;
  activeTripStore: ActiveTripStore;
}
