import React, { FC, useState } from 'react';
import { List, Tabs } from 'antd';
import TabPane from 'antd/es/tabs/TabPane';
import ruRU from 'antd/lib/locale/ru_RU';
import { useNotificationsPassengers } from 'api/notifications';
import { usePagination } from 'shared/hooks/usePagination';

import { notificationClassPassengers } from '../consts';
import { TNotification } from '../Notifications.interface';
import { CheckboxGroup } from '../CheckboxGroup';
import {
  NotificationClassTypes,
  NotificationsTypeHeaders,
  NotificationTypeSettings
} from './Notifications.interface';

import styles from './styles.module.scss';

export const NotificationsPassengers: FC = () => {
  const [notificationClass, setNotificationClass] = useState<string>(notificationClassPassengers);

  const { pageSetting, onPaginationChange } = usePagination({ page: 0, size: 10 });
  const settings = React.useMemo(() => ({
    page: pageSetting.page,
    pageSize: pageSetting.size,
    directionAsc: true,
    sortField: 'NAME',
    class: notificationClass,
  }), [notificationClass, pageSetting.page, pageSetting.size]);

  const { data } = useNotificationsPassengers(settings);
  const {
    content: notificationsList, totalElements, totalPages,
  } = data;

  return (
    <>
      <Tabs
        destroyInactiveTabPane
        className={styles.Tabs}
        onChange={tab => {
          setNotificationClass(tab);
        }}
      >
        {NotificationClassTypes[NotificationTypeSettings.PASSENGERS].map(tabId => (
          <TabPane
            destroyInactiveTabPane={true}
            tab={NotificationsTypeHeaders[tabId]}
            key={tabId}
          >
            <List
              itemLayout="horizontal"
              dataSource={notificationsList}
              pagination={{
                current: pageSetting.page + 1,
                total: totalElements,
                pageSize: pageSetting.size,
                showSizeChanger: true,
                onChange: onPaginationChange,
                onShowSizeChange: onPaginationChange,
                totalBoundaryShowSizeChanger: totalPages,
                locale: ruRU.Pagination,
              }}
              renderItem={(item: TNotification) => {
                return (
                  <List.Item>
                    <List.Item.Meta
                      description={item?.name}
                    />
                    <CheckboxGroup {...item} />
                  </List.Item>
                );
              }}
            />
          </TabPane>
        ))}
      </Tabs>
    </>
  );
};
