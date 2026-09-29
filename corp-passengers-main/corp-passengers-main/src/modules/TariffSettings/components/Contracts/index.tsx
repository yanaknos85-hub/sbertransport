import React from 'react';
import type { FC } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';

import { useTranslation } from 'i18n';
import { TabPane, Tabs } from 'shared/components/Tabs';
import { ContractsTabs } from '../../constants';
import ContractsContent from './ContractsContent';

const Contracts: FC = () => {
  const { t } = useTranslation();
  const { type, routeId } = useParams<{ type: ContractsTabs; routeId: string }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value).replace(':routeId', routeId));
  };

  return (
    <Tabs
      activeKey={type}
      onChange={handleChangeTab}
      destroyInactiveTabPane
    >
      <TabPane key={ContractsTabs.Passengers} tab={t.ServiceType.passengers}>
        <ContractsContent />
      </TabPane>
    </Tabs>
  );
};

export default Contracts;
