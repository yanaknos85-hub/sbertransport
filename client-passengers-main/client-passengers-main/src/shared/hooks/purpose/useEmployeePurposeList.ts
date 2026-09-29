import usePurposeListBase, { PurposeListBase } from './usePurposeListBase';
import { useAppStoreContext } from '../useEmpContext';
import { StoreNames } from 'stores';

/**
 * Хук для получения списка целей по пользователю
 */
const useEmployeePurposeList = (): PurposeListBase => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  return usePurposeListBase(tripStore.purposes);
};

export default useEmployeePurposeList;
