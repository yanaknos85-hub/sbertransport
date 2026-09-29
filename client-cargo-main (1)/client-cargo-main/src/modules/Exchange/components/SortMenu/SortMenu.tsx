import React, { useEffect } from 'react';
import { DownOutlined, UpOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { SortProperty } from '../../types';

import styles from './styles.module.scss';

export const SortMenu: React.FC = () => {
  const { [StoreNames.exchangeStore]: exchangeStore } = useAppStoreContext();
  const {
    sortSetting,
    sortingOrders,
    setSortingOrders,
    setSortSetting,
  } = exchangeStore;

  const sortByParameters = (directionAsc: boolean, property: SortProperty): void => {
    setSortSetting(directionAsc, property);
    setSortingOrders(directionAsc, property);
  };

  useEffect(() => {
    setSortingOrders(sortSetting.directionAsc, sortSetting.property);
  }, [sortSetting]);

  return (
    <div className={styles.dropdownDiv}>
      {!sortSetting.directionAsc ? (
        <Button
          type="text"
          onClick={() => {
            sortByParameters(false, SortProperty.CREATION_DATE);
          }}
          className={styles.dropdownButton}
        >
          {`${sortingOrders}`}
          {' '}
          <DownOutlined />
        </Button>
      ) : (
        <Button
          type="text"
          onClick={() => {
            sortByParameters(true, SortProperty.CREATION_DATE);
          }}
          className={styles.dropdownButton}
        >
          {`${sortingOrders}`}
          {' '}
          <UpOutlined />
        </Button>
      )}
    </div>
  );
};
