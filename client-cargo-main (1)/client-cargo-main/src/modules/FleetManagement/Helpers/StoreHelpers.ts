import { Ioc, IParkings, IRouter } from '../Interfaces';

const getStore: IRouter.TGetStores = (container: Ioc.Container): object => ({
  [Ioc.StoreNames.parkingsStore]: container.get<IParkings.Store>(Ioc.Types.IParkingsStore),
});

export { getStore };
