import { UserOutlined } from '@ant-design/icons';
import {
  Avatar, Button, Dropdown, Menu, Tag
} from 'antd';
import { observer } from 'mobx-react';
import React from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { AppLinksStartPage } from 'constants/constants.app';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import styles from './userMenu.module.scss';
import { getUrlCorpApp } from './utils';

export const UserMenu: React.FC = observer(() => {
  const { [StoreNames.employeeStore]: employeeStore, [StoreNames.settingsStore]: settingsStore } = useAppStoreContext();

  const { menuCounterList } = settingsStore;

  const history = useHistory();
  const match = useRouteMatch();
  const urlOfCorpApp = getUrlCorpApp();

  const toProfile = (): void => {
    history.push(`${match.path}/${EmployeeAppLinks.profile}`);
  };

  const toPersonalCars = (): void => {
    history.push(`${match.path}/${EmployeeAppLinks.personalCars}`);
  };

  const toLogout = (): void => {
    history.push(AppLinksStartPage.Logout);
  };

  const menu = (
    <Menu>
      <Menu.Item>
        <Button
          block={true}
          type="link"
          onClick={toProfile}
          className={styles.button}
        >
          {EmployeeAppLinksTitles.profile}
        </Button>
      </Menu.Item>
      <Menu.Item>
        <Button
          block={true}
          type="link"
          onClick={toPersonalCars}
          className={styles.button}
        >
          {EmployeeAppLinksTitles.personalCars}
        </Button>
      </Menu.Item>
      <Menu.Item>
        <Button
          block={true}
          type="link"
          className={styles.button}
        >
          <a href={urlOfCorpApp}>{EmployeeAppLinksTitles.admin}</a>
        </Button>
      </Menu.Item>
      <Menu.Item>
        <Button
          block={true}
          type="link"
          onClick={toLogout}
          className={styles.button}
        >
          Выйти
        </Button>
      </Menu.Item>
    </Menu>
  );

  return (
    <div style={{
      justifyContent: 'flex-end', paddingRight: 8, alignContent: 'center',
    }}
    >
      <Dropdown overlay={menu}>
        <Button type="link">
          <Tag color="var(--warning-color)">{menuCounterList[EmployeeAppLinks.trips]}</Tag>
          <span>{employeeStore.selfEmployee?.shortName || ''}</span>
        </Button>
      </Dropdown>
      <Avatar size="small" icon={<UserOutlined />} />
    </div>
  );
});
