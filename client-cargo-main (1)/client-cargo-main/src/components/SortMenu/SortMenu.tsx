import React, { useEffect } from 'react';
import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown, MenuProps } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { SortProperty } from './SortConstants';

import styles from './styles.module.scss';

export const SortMenu: React.FC<{
  activeTab: string;
  onSortChange: any;
  sortSetting: { directionAsc: boolean; property: SortProperty };
  name: string;
}> = ({
  onSortChange, sortSetting, name,
}) => {
  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();
  const { setSortingOrders, nameList } = cargosStore;

  const sortByParameters = (directionAsc: boolean, property: SortProperty): void => {
    onSortChange(directionAsc, property);
  };

  useEffect(() => {
    setSortingOrders(sortSetting.directionAsc, sortSetting.property);
  }, [nameList, sortSetting]);

  const items: MenuProps['items'] = [
    {
      type: 'group',
      label: 'По порядковому номеру заявки',
      children: [
        {
          key: 'request_desc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(false, SortProperty.REQUEST_HUMAN_ID)}
            >
              По убыванию
            </Button>
          ),
        },
        {
          key: 'request_asc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(true, SortProperty.REQUEST_HUMAN_ID)}
            >
              По возрастанию
            </Button>
          ),
        },
      ],
    },
    {
      type: 'divider',
    },
    {
      type: 'group',
      label: 'По времени создания',
      children: [
        {
          key: 'creation_desc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(false, SortProperty.CREATION_DATE)}
            >
              По убыванию
            </Button>
          ),
        },
        {
          key: 'creation_asc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(true, SortProperty.CREATION_DATE)}
            >
              По возрастанию
            </Button>
          ),
        },
      ],
    },
    {
      type: 'divider',
    },
    {
      type: 'group',
      label: 'По сроку доставки',
      children: [
        {
          key: 'delivery_desc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(false, SortProperty.DESIRED_DATE)}
            >
              По убыванию
            </Button>
          ),
        },
        {
          key: 'delivery_asc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(true, SortProperty.DESIRED_DATE)}
            >
              По возрастанию
            </Button>
          ),
        },
      ],
    },
    {
      type: 'divider',
    },
    {
      type: 'group',
      label: 'По цене',
      children: [
        {
          key: 'price_desc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(false, SortProperty.EXPECTED_COST)}
            >
              По убыванию
            </Button>
          ),
        },
        {
          key: 'price_asc',
          label: (
            <Button
              className={styles.button}
              block={true}
              type="text"
              onClick={() => sortByParameters(true, SortProperty.EXPECTED_COST)}
            >
              По возрастанию
            </Button>
          ),
        },
      ],
    },
  ];

  return (
    <div className={styles.dropdownDiv}>
      <Dropdown menu={{ items }}>
        <Button
          type="text"
          onClick={e => e.preventDefault()}
          className={styles.dropdownButton}
        >
          {name}
          <DownOutlined />
        </Button>
      </Dropdown>
    </div>
  );
};
