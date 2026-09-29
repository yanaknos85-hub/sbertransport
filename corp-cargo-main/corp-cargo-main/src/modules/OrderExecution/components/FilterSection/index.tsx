import React, { useState } from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';
import { Input, Button } from 'antd';
import { SelectValue } from 'antd/lib/select';
import classNames from 'classnames';
import { useParams } from 'react-router-dom';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Section } from '../ItemContainer';
import { FilterIcon } from '../Icon';
import FilterCategory from '../FilterCategory';
import Filter from '../Filter';
import Modal from '../Filter/components/Modal';
import { useFilterSearch } from './useFilterSearch';
import DeadlineSelect from './DeadlineSelect';

import styles from './styles.module.scss';

const FilterSection: FC = () => {
  const { cargoStore } = useAppStoreContext();
  const { category } = useParams<{ type: string }>();

  const { searchValue, handleInputChange, handleSearchByInitialsName } = useFilterSearch(
    cargoStore,
    category,
  );

  const [isModalVisible, setIsModalVisible] = useState(false);

  const hasDeadlineValue = !!cargoStore.cargoQueryFilters?.criticalExpireDate;

  const handleCriticalExpireDateChange = async (value: SelectValue) => {
    cargoStore.setCargoFilterQueryProps({
      criticalExpireDate: (value as string) || undefined,
    });

    if (category === 'template') {
      await cargoStore.getCargoSchedulerList();
    } else {
      await cargoStore.getCargoOrderListDeferredPost();
    }
  };

  return (
    <Section>
      <FilterCategory />
      {category !== undefined && (
        <div className={styles.filterSection}>
          <div className={classNames(styles.filterSection__item, styles.filterSection__item_search)}>
            <Input.Search
              className={styles.filterSection__search}
              placeholder="Поиск"
              size="large"
              enterButton
              onSearch={handleSearchByInitialsName}
              onChange={handleInputChange}
              allowClear
              value={searchValue}
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
            <Modal
              width="100%"
              visible={isModalVisible}
              footer={null}
              onCancel={() => setIsModalVisible(!isModalVisible)}
            >
              <Filter category={category} onClose={() => setIsModalVisible(false)} />
            </Modal>
            {category !== 'template' && (
              <DeadlineSelect
                value={cargoStore.cargoQueryFilters?.criticalExpireDate}
                hasValue={hasDeadlineValue}
                onChange={handleCriticalExpireDateChange}
              />
            )}
          </div>
        </div>
      )}
    </Section>
  );
};

export default observer(FilterSection);