import { useMemo } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICompensationRequestSearch } from 'stores/Compensations/types';
import { StoreNames } from 'stores/StoreNames.enum';
import { CargosTabsFilters } from 'constants/Cargo.constants';

export const useCompensationFetchConfig = () => {
  const { [StoreNames.compensationStore]: compensationStore } = useAppStoreContext();

  return useMemo(() => ({
    [CargosTabsFilters.active]: (data: ICompensationRequestSearch) => compensationStore.getCompensationListNonTerminal(data),
    [CargosTabsFilters.final]: (data: ICompensationRequestSearch) => compensationStore.getCompensationListTerminal(data),
  }), [compensationStore]);
};
