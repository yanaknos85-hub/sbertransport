import { Container, interfaces } from 'inversify';
import { IRootStore, TYPES } from './stores.types';
import { HttpService } from '../stores/Http/HttpService';
import { IHttpService } from '../stores/Http/http.interface';
import { IResponseService, ResponseService } from '../stores/Http/Response.service';
import { IConfigStore } from '../stores/Config/Config.interface';
import { DIConfigStore } from '../stores/Config/DIConfigStore';
import { INavigator, Navigator } from './Navigator/navigator';
import { IAuthService, IAuthStore } from './Auth/auth.interfaces';
import { Token } from './Token/token';
import { DIAuthService, DIAuthStore } from './Auth';
import { MapStore } from './Map/MapStore';
import { MainLayoutStore } from './MainLayout/MainLayoutStore';
import { ActiveTripStore } from './ActiveTrip/ActiveTripStore';

const createParentContainer = () => {
  const container: Container = new Container();

  container.bind<IResponseService>(TYPES.IResponseService).to(ResponseService);
  container.bind<INavigator>(TYPES.INavigator).to(Navigator);
  container.bind<Token>(TYPES.Token).toConstantValue(new Token());

  container.bind<IConfigStore>(TYPES.IConfigStore).to(DIConfigStore).inSingletonScope();

  container
    .bind<IHttpService>(TYPES.IHttpService)
    .toDynamicValue(
      (context: interfaces.Context) => new HttpService(
        context.container.get<IConfigStore>(TYPES.IConfigStore),
        context.container.get<INavigator>(TYPES.INavigator),
        context.container.get<Token>(TYPES.Token)
      )
    )
    .inSingletonScope();

  container.bind<IAuthService>(TYPES.IAuthService).to(DIAuthService);
  container.bind<IAuthStore>(TYPES.IAuthStore).to(DIAuthStore).inSingletonScope();

  container.bind<MapStore>(TYPES.MapStore).to(MapStore).inSingletonScope();
  container.bind<MainLayoutStore>(TYPES.MainLayoutStore).to(MainLayoutStore).inSingletonScope();
  container.bind<ActiveTripStore>(TYPES.ActiveTripStore).to(ActiveTripStore).inSingletonScope();

  return container;
};

const сreateRootContainer = (): Container => {
  const container: Container = createParentContainer();
  container.parent = new Container();
  return container;
};

export const rootContainer = сreateRootContainer();

export const initRootStore = (rootContainer: Container): IRootStore => {
  return {
    authStore: rootContainer.get<IAuthStore>(TYPES.IAuthStore),
    configStore: rootContainer.get<IConfigStore>(TYPES.IConfigStore),
    http: rootContainer.get<IHttpService>(TYPES.IHttpService),
    process: rootContainer.get<ResponseService>(TYPES.IResponseService),
    mapStore: rootContainer.get<MapStore>(TYPES.MapStore),
    mainLayoutStore: rootContainer.get<MainLayoutStore>(TYPES.MainLayoutStore),
    activeTripStore: rootContainer.get<ActiveTripStore>(TYPES.ActiveTripStore),
    // Добавить ребилд на будущее
  } as IRootStore;
};
