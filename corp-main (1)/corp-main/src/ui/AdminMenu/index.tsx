import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';
import {
  Avatar,
  Button,
  Dropdown,
  Menu
} from 'antd';
import { UserOutlined } from '@ant-design/icons';

import { useProfile } from 'api/profile';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useAdminMenu } from './useAdminMenu';

import styles from './admin-menu.module.scss';
import { Notifications } from '../Notifications';

export enum AdminHeaderLinks {
  ClientApp = 'ClientApp',
  Exit = 'Exit',
}

export const AdminMenu: React.FC = observer(() => {
  const profile = useProfile().data;

  const { authStore } = useAppStoreContext();
  const { toClient } = useAdminMenu();

  const { t } = useTranslation();

  const onClick: { [key in AdminHeaderLinks]: () => void } = useMemo(
    () => ({
      [AdminHeaderLinks.ClientApp]: () => {
        window.location.href = toClient;
      },
      [AdminHeaderLinks.Exit]: () => authStore.logout(),
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [toClient, authStore.logout]
  );

  const menu = useMemo(
    () => (
      <Menu>
        {Object.values(AdminHeaderLinks).map(link => (
          <Menu.Item onClick={onClick[link]} key={link}>
            <Button type="link">{t.AdminMenu[link]}</Button>
          </Menu.Item>
        ))}
      </Menu>
    ),
    [onClick, t]
  );

  return (
    <div className={styles.container}>
      <Notifications />
      <Dropdown overlay={menu} align={{ overflow: { adjustX: true, adjustY: true } }}>
        <Button type="link" className={styles.menuButton}>
          {`${profile.firstName.charAt(0)}. ${profile.patronymic ? `${profile.patronymic.charAt(0)}. ` : ''}${profile.lastName}`}
        </Button>
      </Dropdown>
      <Avatar size="default" icon={<UserOutlined />} />
    </div>
  );
});
