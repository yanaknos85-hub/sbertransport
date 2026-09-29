import usePurposeListBase, { PurposeListBase } from './usePurposeListBase';
import { StoreNames } from 'stores';
import { useAppStoreContext } from '../useEmpContext';

/**
 * Хук для получения списка целей
 */
const useAllPurposeList = (): PurposeListBase => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  return usePurposeListBase(tripStore.purposes);
};

export default useAllPurposeList;
