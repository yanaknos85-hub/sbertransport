import React, { FC, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { Tabs } from '@sber-sbertransport/ui-kit/src';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { EXCHANGE } from 'constants/constants.routes';

import { useGetFilters } from './api/filters';
import { List } from './components/List/List';
import { ContractsTabs } from './types';

import styles from './Exchage.module.scss';

interface Props {
  list: string[];
}

const LogisticsExchange: FC<Props> = observer(() => {
  const history = useHistory();
  const { params: { type } } = useRouteMatch();

  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const {
    getAvailableListWithFilters,
    pageSetting,
    sortSetting,
  } = exchangeStore;

  const { data: savedFilters } = useGetFilters();

  const handleTabChange = (newType: string) => {
    history.push(`${EXCHANGE}/${newType}`);
    // Сброс состояния пагинации при переключении списка
    exchangeStore.resetSettings();
  };

  const tabItems = [
    { key: ContractsTabs.AVAILABLE, label: 'Доступные заявки' },
    { key: ContractsTabs.NON_TERMINAL, label: 'Заявки в работе' },
    { key: ContractsTabs.TERMINAL, label: 'Завершенные заявки' },
  ];

  useEffect(() => {
    const addressFrom = savedFilters?.addressFrom;
    const addressTo = savedFilters?.addressTo;

    getAvailableListWithFilters(type, pageSetting, sortSetting, addressFrom, addressTo);
  }, [type, pageSetting, sortSetting, getAvailableListWithFilters]);

  return (
    <div className={styles.layout}>
      <div className={styles.cargoLayout}>
        <Tabs
          onChange={handleTabChange}
          activeKey={type}
          className={styles.tabs}
          items={tabItems}
        />
        <List />
      </div>
    </div>
  );
});

export default LogisticsExchange;
