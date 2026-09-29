import { useAppStore, StoreNames } from 'stores';

export const useInitStore = () => {
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStore();

  const isSelfEmployeeLoaded = !!selfStore.selfEmployee?.userId;

  return {
    isSelfEmployeeLoaded,
  };
};
