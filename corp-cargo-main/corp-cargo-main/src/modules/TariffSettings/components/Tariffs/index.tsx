import React from 'react';
import { useParams, useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { TabPane, Tabs } from 'shared/components/Tabs';
import { TariffsTabs } from '../../constants';
import TariffsContent from './TariffsContent';

const Tariffs = () => {
  const { type, routeId } = useParams<{ type: TariffsTabs; routeId: string; transportType: string }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value).replace(':routeId', routeId).replace('/:transportType', ''));
  };

  return (
    <Tabs
      activeKey={type}
      onChange={handleChangeTab}
      destroyInactiveTabPane
    >
      <TabPane key={TariffsTabs.Cargo} tab="Грузовые перевозки">
        <TariffsContent />
      </TabPane>
    </Tabs>
  );
};

export default Tariffs;
