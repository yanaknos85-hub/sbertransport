import React, { useState } from 'react';
import { observer } from 'mobx-react';
import { TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICargoRequestSearch } from 'stores/Cargos/Cargos.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import {
  activeLabelName,
  CargosTabsFilters,
  finalLabelName
} from 'constants/Cargo.constants';

import CargosJournal from '../CargoJournal/CargosJournal';

const CargosApprovalJournal: React.FC<{
  isRegular?: boolean;
}> = observer(({ isRegular = false }) => {
  const {
    [StoreNames.cargosStore]: cargosStore,
  } = useAppStoreContext();

  const statusTabs: TabsNavOption[] = [
    {
      key: CargosTabsFilters.active,
      label: activeLabelName,
    },
    {
      key: CargosTabsFilters.final,
      label: finalLabelName,
    },
  ];

  const [statusTabsState, setStatusTabsState] = useState(statusTabs);

  const fetchDataConfig = {
    [statusTabsState[0].key]: (data: ICargoRequestSearch) => isRegular ? cargosStore.getMultipleRegularApproveListNonTerminal(data) : cargosStore.getMultipleApproveListNonTerminal(data),
    [statusTabsState[1].key]: (data: ICargoRequestSearch) => isRegular ? cargosStore.getMultipleRegularApproveListTerminal(data) : cargosStore.getMultipleApproveListTerminal(data),
  };

  return (
    <CargosJournal
      tabs={statusTabsState}
      setTabs={setStatusTabsState}
      activeLabelName={activeLabelName}
      finalLabelName={finalLabelName}
      fetchDataConfig={fetchDataConfig}
      isApproval={true}
      isRegular={isRegular}
    />
  );
});

export default CargosApprovalJournal;
