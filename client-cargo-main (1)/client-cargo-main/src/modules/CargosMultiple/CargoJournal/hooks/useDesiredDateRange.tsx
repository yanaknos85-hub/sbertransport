import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export const useCreationDateRange = () => {
  const {
    [StoreNames.cargosStore]: cargosStore,
  } = useAppStoreContext();

  const start = cargosStore?.desiredDateRange && cargosStore?.desiredDateRange[0];
  const end = cargosStore?.desiredDateRange && cargosStore?.desiredDateRange[1];
  return start && end ? ({
    start: start ? start?.utcOffset(0).startOf('day').toISOString() : null,
    end: end ? end?.utcOffset(0).endOf('day').toISOString() : null,
  }) : undefined;
};
