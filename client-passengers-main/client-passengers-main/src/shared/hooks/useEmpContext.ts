// FixMe: Оставил это тут, чтобы тупо не менять пути, иначе можно было бы обращаться напрямую к useStores
import { IAppStore, useAppStore } from 'stores';

export function useAppStoreContext(): IAppStore {
  const stores = useAppStore();
  if (!stores) {
    throw new Error('There was an attemption of Stores usage before initiation.');
  }
  return stores;
}
