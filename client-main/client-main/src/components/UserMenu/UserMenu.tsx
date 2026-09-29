import { UserOutlined } from '@ant-design/icons';
import {
  Avatar, Button, Dropdown, Menu, Tag
} from 'antd';
import { observer } from 'mobx-react';
import React, { useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { EmployeeAppLinksTitles, ROLE } from 'constants/constants.app';
import { useRole } from 'utils/useRole';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useUrlCorpApp } from 'shared/hooks/useUrlCorpApp';
import { StoreNames } from 'stores/StoreNames.enum';
import { hooks } from 'stores';

import styles from './userMenu.module.scss';

export const UserMenu: React.FC = observer(() => {
  const {
    [StoreNames.tripStore]: tripStore, [StoreNames.employeeStore]: employeeStore,
  } = useAppStoreContext();
  const { loadCountActiveApprovals } = tripStore;

  const { isSelfEmployeeLoaded } = hooks.useInitStore();

  const history = useHistory();
  const match = useRouteMatch();
  const { urlOfCorpApp } = useUrlCorpApp();
  const roles = useRole();

  const toProfile = (): void => {
    history.push(`${match.path}/profile`);
  };

  const toPersonalCars = (): void => {
    history.push(`passengers/personalCars`);
  };

  const toLogout = (): void => {
    history.push('/oauth/logout');
  };

  useEffect(() => {
    if (isSelfEmployeeLoaded) {
      loadCountActiveApprovals();
      window.addEventListener('approve', loadCountActiveApprovals);
      window.addEventListener('decline', loadCountActiveApprovals);

      return () => {
        window.removeEventListener('approve', loadCountActiveApprovals);
        window.removeEventListener('decline', loadCountActiveApprovals);
      };
    }
  }, [isSelfEmployeeLoaded]);

  const menu = (
    <Menu>
      <Menu.Item key="1">
        <Button
          block={true}
          type="link"
          onClick={toProfile}
          className={styles.button}
        >
          {EmployeeAppLinksTitles.profile}
        </Button>
      </Menu.Item>
      <Menu.Item key="2">
        <Button
          block={true}
          type="link"
          onClick={toPersonalCars}
          className={styles.button}
        >
          {EmployeeAppLinksTitles.personalCars}
        </Button>
      </Menu.Item>
      {!(roles.length === 1 && roles.includes(ROLE.EMPLOYEE_CORP_CLIENT)) && (
        <Menu.Item key="3">
          <Button
            block={true}
            type="link"
            className={styles.button}
          >
            <a href={urlOfCorpApp}>{EmployeeAppLinksTitles.admin}</a>
          </Button>
        </Menu.Item>
      )}
      <Menu.Item key="4">
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
          <Tag color="var(--warning-color)">{tripStore?.countActiveApprovals}</Tag>
          <span>{employeeStore.selfEmployee?.shortName || ''}</span>
        </Button>
      </Dropdown>
      <Avatar size="small" icon={<UserOutlined />} />
    </div>
  );
});
