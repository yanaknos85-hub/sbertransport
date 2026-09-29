import React, { useEffect, useState } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { Tabs } from 'antd';
import { observer } from 'mobx-react';
import CargoLayout, { CargoLayoutWrapper } from 'shared/components/Cargo/CargoLayout';
import { TabsNavOption } from 'shared/components/TabsNav';

import { CargosTabsFilters } from 'constants/Cargo.constants';
import {
  APPROVEMENT_CARGOS_JOURNAL,
  APPROVEMENT_COMPENSATION_JOURNAL
} from 'constants/constants.routes';

import CompensationApprovalJournal from '../Compensation/CompensationApprovalJournal';
import CargoApprovalJournal from './CargoApprovalJournal';

const CargosApprovalTabs: React.FC = observer(() => {
  const history = useHistory();
  const location = useLocation();

  const tabs: TabsNavOption[] = [
    { key: CargosTabsFilters.cargo, label: 'Доставки' },
    { key: CargosTabsFilters.compensation, label: 'Компенсации' },
  ];

  // Определяем активную вкладку из текущего пути
  const [activeKey, setActiveKey] = useState(() => {
    return location.pathname.includes('compensation')
      ? CargosTabsFilters.compensation
      : CargosTabsFilters.cargo;
  });

  // Синхронизируем вкладку с URL при изменении пути
  useEffect(() => {
    const key = location.pathname.includes('compensation')
      ? CargosTabsFilters.compensation
      : CargosTabsFilters.cargo;

    if (key !== activeKey) {
      setActiveKey(key);
    }
  }, [location.pathname, activeKey]);

  const handleTabChange = (key: any) => {
    setActiveKey(key);

    if (key === CargosTabsFilters.compensation) {
      history.push(APPROVEMENT_COMPENSATION_JOURNAL.replace(':filter', 'active'));
    } else {
      history.push(APPROVEMENT_CARGOS_JOURNAL.replace(':filter', 'active'));
    }
  };

  const renderContent = () => {
    if (location.pathname.includes('compensation')) {
      return <CompensationApprovalJournal />;
    } else {
      return <CargoApprovalJournal />;
    }
  };

  return (
    <CargoLayoutWrapper>
      <CargoLayout>
        {/* Вкладки: Доставки / Компенсации */}
        <Tabs
          items={tabs}
          activeKey={activeKey}
          onChange={handleTabChange}
        />

        <div style={{ marginTop: '16px' }}>
          {renderContent()}
        </div>
      </CargoLayout>
    </CargoLayoutWrapper>
  );
});

export default CargosApprovalTabs;
