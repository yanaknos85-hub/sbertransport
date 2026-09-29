import { useMemo } from 'react';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICargoRequestSearch } from 'stores/Cargos/Cargos.interface';
import { CargosTabsFilters } from 'constants/Cargo.constants';

export const useCargoApprovalFetchConfig = (isRegular: boolean) => {
  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();

  return useMemo(() => {
    return {
      [CargosTabsFilters.active]: (data: ICargoRequestSearch) => isRegular
        ? cargosStore.getMultipleRegularApproveListNonTerminal(data)
        : cargosStore.getMultipleApproveListNonTerminal(data),
      [CargosTabsFilters.final]: (data: ICargoRequestSearch) => isRegular
        ? cargosStore.getMultipleRegularApproveListTerminal(data)
        : cargosStore.getMultipleApproveListTerminal(data),
    };
  }, [isRegular, cargosStore]);
};
