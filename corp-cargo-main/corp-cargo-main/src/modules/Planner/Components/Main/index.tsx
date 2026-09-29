import React, { useMemo, lazy } from 'react';
import { observer } from 'mobx-react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useLocation } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { TabPane } from 'shared/components/Tab/TabPane';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import Planner from 'modules/Planner/Components/Planner/Planner';
import RouteTable from 'modules/Planner/Components/Monitor/RouteTable';
import { Tab } from 'modules/Planner/types';

const EtrnSignature = lazy(() => import('modules/Planner/Components/EtrnSignature/EtrnSignature'));

import * as S from './Main.styles';

const Main = () => {
  const { plannerStore } = useAppStoreContext();
  const location = useLocation();
  const history = useHistory();

  // Синхронизация activeTab из URL до рендера Tabs (useMemo — синхронно, в отличие от useEffect).
  // Это гарантирует, что при обновлении страницы на вкладке "Подписание ЭТрН" (?tab=etrn)
  // activeTab будет установлен до того, как отрендерятся дочерние компоненты (Planner, RouteTable).
  // Без этого Planner успевал отправить запрос useSearchRoutes до синхронизации таба.
  useMemo(() => {
    const searchParams = new URLSearchParams(location.search);
    const tabParam = searchParams.get('tab');

    if (tabParam === 'journal') {
      if (plannerStore.activeTab !== Tab.journal) plannerStore.setActiveTabKey(Tab.journal);
    } else if (tabParam === 'etrn') {
      if (plannerStore.activeTab !== Tab.etrn) plannerStore.setActiveTabKey(Tab.etrn);
    } else if (plannerStore.activeTab !== Tab.planner) {
      plannerStore.setActiveTabKey(Tab.planner);
    }
  }, [location.search]);

  const onChange = (activeKey: Tab) => {
    plannerStore.setActiveTabKey(activeKey);
    // Сбрасываем параметры URL при переключении вкладок
    if (activeKey === Tab.planner) {
      history.push(routes.PLANNER_JOURNAL);
    } else if (activeKey === Tab.journal) {
      history.push(`${routes.PLANNER_JOURNAL}?tab=journal`);
    } else if (activeKey === Tab.etrn) {
      history.push(`${routes.PLANNER_JOURNAL}?tab=etrn`);
    }
  };

  return (
    <S.TabsStyled
      onChange={onChange}
      defaultActiveKey={Tab.planner}
      activeKey={plannerStore.activeTab}
    >
      <TabPane tab="Планировщик" key={Tab.planner}>
        <Planner />
      </TabPane>
      <TabPane tab="Журнал маршрутов" key={Tab.journal}>
        <RouteTable />
      </TabPane>
      <TabPane tab="Подписание ЭТрН" key={Tab.etrn}>
        <EtrnSignature />
      </TabPane>
    </S.TabsStyled>
  );
};

export default observer(Main);
