import { Popconfirm } from 'antd';
import React, { useCallback, useEffect } from 'react';
import { EditableTable } from 'shared/components/EditableTable';
import {
  NotificationClass,
  NotificationClassTypes,
  NotificationsTypeHeaders,
  NotificationTypeSettings
} from 'stores/Notifications/Notifications.interface';

import { useTranslation } from 'i18n';
import { useHistory } from 'react-router-dom';
import { HISTORY_PUSH_DELAY } from 'shared/constants/forms.constants';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import { Button } from 'shared/components/Button/Button';
import { useColumns } from './components/useColumns';
import { useNotificationsSettings } from './components/useNotificationsSettings';

import styles from './styles.module.scss';

const NotificationsSettings = () => {
  const { t } = useTranslation();

  const {
    store,
    setNotificationClass,
    setToDefault,
    notificationClass,
    updateOrCreateNotificationMass,
  } = useNotificationsSettings();

  const history = useHistory();
  const goBack = useCallback(() => setTimeout(() => history.push('./'), HISTORY_PUSH_DELAY), [history]);
  const columns = useColumns(notificationClass);

  useEffect(() => {
    setNotificationClass(NotificationClass.REQUEST_TAXI);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const saveButton = (
    <Popconfirm
      placement="top"
      title={t.NotificationsSettings.saveSettings}
      onConfirm={updateOrCreateNotificationMass}
      okText={t.BooleanField.trueLabel}
      cancelText={t.BooleanField.falseLabel}
      style={{ width: 300 }}
    >
      <Button type="primary">{t.global.save}</Button>
    </Popconfirm>
  );

  const cancelButton = (
    <Popconfirm
      placement="top"
      title={t.NotificationsSettings.cancelSettings}
      onConfirm={goBack}
      okText={t.BooleanField.trueLabel}
      cancelText={t.BooleanField.falseLabel}
      style={{ width: 300 }}
    >
      <Button danger type="primary">
        {t.global.cancel}
      </Button>
    </Popconfirm>
  );

  const defaultButton = (
    <Popconfirm
      placement="top"
      title={t.NotificationsSettings.defaultSettings}
      onConfirm={setToDefault}
      okText={t.NotificationsSettings.set}
      cancelText={t.global.cancel}
      style={{ width: 300 }}
    >
      <Button className={styles.backToDefaults} type="primary">
        {t.global.defaultValues}
      </Button>
    </Popconfirm>
  );

  return (
    <>
      <Tab
        className={styles.Tabs}
        onChange={tab => {
          store.stopEditingAll();
          setNotificationClass(tab as NotificationClass);
        }}
        destroyInactiveTabPane
      >
        {NotificationClassTypes[NotificationTypeSettings.PASSENGERS].map(tabId => (
          <TabPane
            tab={NotificationsTypeHeaders[tabId]}
            key={tabId}
            theme="card"
          >
            <EditableTable
              className={styles.tableLayout}
              store={store}
              columns={columns}
              pagination={false}
              style={{ overflow: 'scroll' }}
              scroll={{ x: 1200 }}
              actionsSize={80}
              tableLayout="fixed"
            />
          </TabPane>
        ))}
      </Tab>
      <div className={styles.buttons_container}>
        {defaultButton}
        {cancelButton}
        {saveButton}
      </div>
    </>
  );
};

export default NotificationsSettings;
