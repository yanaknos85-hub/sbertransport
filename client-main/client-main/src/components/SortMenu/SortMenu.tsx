import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown, Menu } from 'antd';
import React from 'react';

import { SortProperty } from './SortConstants';

import styles from './styles.module.scss';

export const SortMenu: React.FC<{
  onSortChange: any;
}> = ({ onSortChange }) => {
  const [label, setLabel] = React.useState<string>('По времени создания: Сначала новые');

  const sortByParameters = (directionAsc: boolean, property: string): void => {
    onSortChange(directionAsc, property);
    switch (property) {
      case SortProperty.REQUEST_HUMAN_ID:
        if (directionAsc) {
          setLabel('По порядковому номеру заявки :Сначала старые');
        } else {
          setLabel('По порядковому номеру заявки :Сначала новые');
        }
        break;
      case SortProperty.CREATION_DATE:
        if (directionAsc) {
          setLabel('По времени создания :Сначала старые');
        } else {
          setLabel('По времени создания :Сначала новые');
        }
        break;
      case SortProperty.DESIRED_DATE:
        if (directionAsc) {
          setLabel('По сроку доставки :Сначала долгие');
        } else {
          setLabel('По сроку доставки :Сначала быстрые');
        }
        break;
      case SortProperty.EXPECTED_COST:
        if (directionAsc) {
          setLabel('По цене :Сначала дешевые');
        } else {
          setLabel('По цене :Сначала дорогие');
        }
        break;
      default:
        break;
    }
  };

  const menu = (
    <Menu>
      <Menu.ItemGroup title="По порядковому номеру заявки">
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.REQUEST_HUMAN_ID)}
          >
            Сначала новые
          </Button>
        </Menu.Item>
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.REQUEST_HUMAN_ID)}
          >
            Сначала старые
          </Button>
        </Menu.Item>
      </Menu.ItemGroup>
      <Menu.Divider />
      <Menu.ItemGroup title="По времени создания">
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.CREATION_DATE)}
          >
            Сначала новые
          </Button>
        </Menu.Item>
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.CREATION_DATE)}
          >
            Сначала старые
          </Button>
        </Menu.Item>
      </Menu.ItemGroup>
      <Menu.Divider />
      <Menu.ItemGroup title="По сроку доставки">
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.DESIRED_DATE)}
          >
            Сначала быстрые
          </Button>
        </Menu.Item>
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.DESIRED_DATE)}
          >
            Сначала долгие
          </Button>
        </Menu.Item>
      </Menu.ItemGroup>
      <Menu.Divider />
      <Menu.ItemGroup title="По цене">
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.EXPECTED_COST)}
          >
            Сначала дорогие
          </Button>
        </Menu.Item>
        <Menu.Item>
          <Button
            className={styles.button}
            block={true}
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.EXPECTED_COST)}
          >
            Сначала дешевые
          </Button>
        </Menu.Item>
      </Menu.ItemGroup>
    </Menu>
  );

  return (
    <div className={styles.dropdownDiv}>
      <Dropdown overlay={menu}>
        <Button
          type="text"
          onClick={e => e.preventDefault()}
          className={styles.dropdownButton}
        >
          {label}
          {' '}
          <DownOutlined />
        </Button>
      </Dropdown>
    </div>
  );
};
