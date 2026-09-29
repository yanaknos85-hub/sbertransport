import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';
import { ReactComponent as Bell } from 'shared/assets/svg/bell.svg';
import { ReactComponent as NotificationBell } from 'shared/assets/svg/notification-bell.svg';
import { IconButton } from 'shared/form/Button/Button';
import { Drawer } from 'shared/components/Drawer';
import emptyFolder from 'shared/images/empty-folder.png';
import { Option, Select } from 'shared/components/Select';
import { SearchPanel } from 'shared/components/SearchPanel/SearchPanel';
import { Space } from 'antd';
import Title from 'antd/lib/typography/Title';
import Text from 'antd/lib/typography/Text';
import styles from './notifications.module.scss';

const NotificationsContent: FC = () => {
  const { t } = useTranslation();

  return (
    <div className={styles.notificationContent}>
      <Space direction="vertical" size="middle">
        <SearchPanel placeholder={t.NotificationCenter.filters.search} />
        <Select value="all">
          <Option value="all">Все</Option>
        </Select>
      </Space>
      <div className={styles.notificationsEmpty}>
        <img src={emptyFolder} alt="" />
        <Title level={5}>{t.NotificationCenter.empty.title}</Title>
        <Text type="secondary">
          {t.NotificationCenter.empty.subTitle1}
          <br />
          {t.NotificationCenter.empty.subTitle2}
        </Text>
      </div>
    </div>
  );
};

const NOTIFICATIONS_LENGTH = 0;

export const Notifications: FC = () => {
  const { t } = useTranslation();

  const [isOpened, setIsOpened] = useState(false);

  const openNotifications = () => {
    setIsOpened(true);
  };

  const onClose = () => {
    setIsOpened(false);
  };

  return (
    <>
      <IconButton
        src={NOTIFICATIONS_LENGTH ? <NotificationBell /> : <Bell />}
        className={styles.notificationButton}
        onClick={openNotifications}
      />
      <Drawer
        title={t.NotificationCenter.title}
        visible={isOpened}
        onClose={onClose}
        destroyOnClose
        width={400}
      >
        <NotificationsContent />
      </Drawer>
    </>
  );
};
