import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TTaxiClass } from 'stores/Trip/Trip.interface';

export const useUserAvailableClasses = (): {
  availableClasses: TTaxiClass[] | undefined;
} => {
  const { corporateStore, selfStore } = useAppStoreContext();

  const detailedSelfEmployee = selfStore;
  // @ts-ignore
  const targetPosition = corporateStore.positions.find(item => item.id === detailedSelfEmployee?.posId);
  const availableClasses = targetPosition?.availableClasses;

  return { availableClasses };
};
