import React, { useState } from 'react';
import { observer } from 'mobx-react';
import { TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICargoRequestSearch } from 'stores/Cargos/Cargos.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import {
  activeLabelName,
  CargosTabsFilters,
  finalLabelName,
  ordersLabelName,
  regularLabelName
} from 'constants/Cargo.constants';

import CargosJournal from './CargoJournal/CargosJournal';
import RequestIconTab from './static/images/requestTab.png';
import ScheduleIconTab from './static/images/scheduleTab.png';

import styles from './styles.module.scss';

const CargosRequestJournal: React.FC<{ isRegular?: boolean }> = observer(({ isRegular }) => {
  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();

  const tabsMain: TabsNavOption[] = [
    {
      key: CargosTabsFilters.once,
      label: (
        <div className={styles.imgWrapper}>
          <img
            className={styles.img}
            src={RequestIconTab}
            alt="tabOrder"
          />
          {ordersLabelName}
        </div>
      ),
    },
    {
      label: (
        <div className={styles.imgWrapper}>
          <img
            className={styles.img}
            src={ScheduleIconTab}
            alt="tabSchedule"
          />
          {regularLabelName}
        </div>
      ),
      key: CargosTabsFilters.regular,
    },
  ];

  const defaultTabs: TabsNavOption[] = [
    {
      key: CargosTabsFilters.active,
      label: activeLabelName,
    },
    {
      key: CargosTabsFilters.final,
      label: finalLabelName,
    },
  ];

  const [tabs, setTabs] = useState(defaultTabs);
  const fetchDataConfig = {
    [tabs[0].key]: (data: ICargoRequestSearch) => isRegular ? cargosStore.getMultipleRegularRequestListNonTerminal(data) : cargosStore.getMultipleRequestListNonTerminal(data),
    [tabs[1].key]: (data: ICargoRequestSearch) => isRegular ? cargosStore.getMultipleRegularRequestListTerminal(data) : cargosStore.getMultipleRequestListTerminal(data),
  };

  return (
    <CargosJournal
      tabs={tabs}
      setTabs={setTabs}
      tabsMain={tabsMain}
      activeLabelName={activeLabelName}
      finalLabelName={finalLabelName}
      fetchDataConfig={fetchDataConfig}
      isApproval={false}
      isRegular={isRegular}
    />
  );
});

export default CargosRequestJournal;
