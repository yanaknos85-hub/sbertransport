import './override.scss';
import React from 'react';
import classNames from 'classnames';
import { NotificationTypeSettings } from 'stores/Notifications/Notifications.interface';
import { useTranslation } from 'i18n';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';

import NotificationsSettingsContent from './NotificationsSettingsContent';

import styles from './styles.module.scss';

const NotificationsSettings = () => {
  const { t } = useTranslation();
  const { DeadlineSettings } = t.ServiceParamsPage;

  return (
    <div className={classNames(styles.container)}>
      <Tab>
        <TabPane
          tab={DeadlineSettings.cargo}
          key={NotificationTypeSettings.CARGO}
        >
           <NotificationsSettingsContent />
        </TabPane>
      </Tab>
    </div>
  );
};

export default NotificationsSettings;
