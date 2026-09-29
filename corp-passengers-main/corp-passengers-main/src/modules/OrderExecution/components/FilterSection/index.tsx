import React, { useState, useEffect } from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';
import { Input, Button } from 'antd';
import classNames from 'classnames';

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Section } from '../ItemContainer';
import { FilterIcon } from '../Icon';
import FilterCategory from '../FilterCategory';
import Filter from '../Filter';
import Modal from '../Filter/components/Modal';
import { useProfile } from 'api/profile';

import styles from './styles.module.scss';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { useTableFields } from '../OrderTable/Passengers/useTableFields';
import { useColumnVisibilitySettings } from '../../hooks/useColumnVisibilitySettings';
import type { OrderExecutionColumnProps } from '../OrderTable/Passengers/types';

interface IProps {
  columns: OrderExecutionColumnProps[];
}

const FilterSection: FC<IProps> = ({ columns }) => {
  const { passengerStore } = useAppStoreContext();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const { userId } = useProfile().data;
  const fields = useTableFields();

  const handleSearchByInitialsName = (searchString: string) => {
    const targetString = searchString.trim();

    if (targetString.length >= 3) {
      passengerStore.setPersonFilterQueryProps({
        id: targetString,
        transportType: passengerStore.personQueryFilters.transportType,
      });

      passengerStore.isOrganization
        ? passengerStore.getPersonOrderList()
        : passengerStore.getPersonOrderListExec();
    } else if (targetString.length === 0) {
      passengerStore.setPersonFilterQueryProps({ transportType: passengerStore.personQueryFilters.transportType });
      passengerStore.getPersonOrderListExec();
    }
  };

  const {
    userSettings,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  useEffect(() => {
    setUserColumnVisibilitySettings(userSettings);
  }, [userSettings, setUserColumnVisibilitySettings]);

  return (
    <Section>
      <FilterCategory />
      <div className={styles.filterSection}>
        <div className={classNames(styles.filterSection__item, styles.filterSection__item_search)}>
          <Input.Search
            className={styles.filterSection__search}
            placeholder="Поиск"
            size="large"
            enterButton
            onSearch={handleSearchByInitialsName}
          />
          <Button
            className={styles.filterSection__filterButton}
            type="primary"
            icon={<FilterIcon />}
            size="large"
            onClick={() => setIsModalVisible(!isModalVisible)}
          >
            Фильтры
          </Button>
          {isModalVisible && (
          <Modal
            width="100%"
            visible={isModalVisible}
            footer={null}
            onCancel={() => setIsModalVisible(!isModalVisible)}
          >
            <Filter onClose={() => setIsModalVisible(false)} />
          </Modal>
          )}
        </div>
        <ColumnVisibilitySettings
          setting={columns}
          defaultColumns={fields}
          saveSettingChange={handleSaveColumnVisibilitySettings as (key?: Record<string, boolean | undefined>) => void}
          transportType={passengerStore.personQueryFilters.transportType}
          isOto={true}
        />
      </div>
    </Section>
  );
};

export default observer(FilterSection);
