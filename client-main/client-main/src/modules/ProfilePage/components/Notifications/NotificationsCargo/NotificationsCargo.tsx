import React, { FC } from 'react';
import { List } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU';
import { TNotification } from '../Notifications.interface';
import { useNotificationsCargo } from 'api/notifications';
import { CheckboxGroup } from '../CheckboxGroup';
import { usePagination } from 'shared/hooks/usePagination';
import { notificationClassCargo } from '../consts';

export const NotificationsCargo: FC = () => {
  const { pageSetting, onPaginationChange } = usePagination({ page: 0, size: 10 });

  const settings = React.useMemo(() => ({
    page: pageSetting.page,
    pageSize: pageSetting.size,
    directionAsc: true,
    sortField: 'NAME',
    class: notificationClassCargo,
  }), [pageSetting]);

  const { data } = useNotificationsCargo(settings);

  const {
    content: notificationsList, totalElements, totalPages,
  } = data;

  return (
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
  );
};
