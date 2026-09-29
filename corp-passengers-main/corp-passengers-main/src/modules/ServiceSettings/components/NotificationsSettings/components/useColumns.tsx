import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { EditableColumnType } from 'shared/components/EditableTable';
import { TextCell } from 'shared/components/Table/TextCell';
import {
  NotificationClass,
  NotificationMapper,
  NotificationsRecord,
  NotificationType
} from 'stores/Notifications/Notifications.interface';

import { SelectApproveFrequency } from './cell-components/SelectApproveFrequency';
import { SelectChannelsCell } from './cell-components/SelectChannelsCell';
import { SelectLowLimit } from './cell-components/SelectLowLimit';
import { SelectRestrictions } from './cell-components/SelectRestrictions';
import { SelectStatusFrequency } from './cell-components/SelectStatusFrequency';
import { SelectWaitingTime } from './cell-components/SelectWaitingTime';

export const useColumns = (notificationClass: NotificationClass): EditableColumnType<NotificationsRecord>[] => {
  const { t } = useTranslation();

  const existedColumns = new Set(NotificationMapper[notificationClass]);

  const renderIfTypeExist = (type: NotificationType) => (record: EditableColumnType<NotificationsRecord>) => {
    if (!existedColumns.has(type)) {
      return null;
    }

    return record;
  };

  const columns = useMemo(
    (): EditableColumnType<NotificationsRecord>[] => ([
      {
        title: t.Notifications.tableHeaders.notification,
        dataIndex: 'notification',
        key: 'notification',
        width: 300,
        render: ({ cellValue }) => cellValue,
        fixed: 'left',
      },
      {
        title: t.Notifications.tableHeaders.pushNotification,
        dataIndex: 'pushNotification',
        key: 'pushNotification',
        width: 250,
        render: props => <TextCell {...props} isEditingRecord={false} />,
      },
      {
        title: t.Notifications.tableHeaders.smsNotification,
        dataIndex: 'smsNotification',
        key: 'smsNotification',
        width: 250,
        render: props => <TextCell {...props} isEditingRecord={false} />,
      },
      {
        title: t.Notifications.tableHeaders.outlookNotification,
        dataIndex: 'outlookNotification',
        key: 'outlookNotification',
        width: 250,
        render: props => <TextCell {...props} isEditingRecord={false} />,
      },
      {
        title: t.Notifications.tableHeaders.enableHeaders,
        dataIndex: 'enabledChannels',
        key: 'enabledChannels',
        width: 250,
        render: props => <SelectChannelsCell {...props} />,
      },
      {
        title: t.Notifications.tableHeaders.restrictions,
        dataIndex: 'restrictions',
        key: 'restrictions',
        width: 250,
        render: props => <SelectRestrictions {...props} />,
      },
      // Additional fields for limits and taxi
      renderIfTypeExist(NotificationType.WAYPOINT_WAITING_EXPIRED)({
        title: t.Notifications.tableHeaders.excessWaitingInIntermediate,
        dataIndex: 'countings',
        key: 'countings',
        width: 250,
        render: props => props.record.notificationType === NotificationType.WAYPOINT_WAITING_EXPIRED && (
          <SelectWaitingTime {...props} isEditingRecord={false} />
        ),
      }),
      renderIfTypeExist(NotificationType.LOW_REMAINS)({
        title: t.Notifications.tableHeaders.lowLimitNotification,
        dataIndex: 'countings',
        key: 'countings',
        width: 250,
        render: props => [NotificationType.LOW_REMAINS, NotificationType.LOW_REMAINS_FROM_EMPLOYEE].includes(
          props.record.notificationType
        ) && <SelectLowLimit {...props} isEditingRecord={false} />,
      }),
      // Additional fields for all approve requests
      renderIfTypeExist(NotificationType.APPROVE)({
        title: t.Notifications.tableHeaders.approvementFrequency,
        dataIndex: 'timings',
        key: 'timings',
        width: 250,
        render: props => (
          props.record.notificationType === NotificationType.APPROVE && (
            <SelectApproveFrequency {...props} isEditingRecord={false} />
          )),
      }),
      renderIfTypeExist(NotificationType.APPROVE)({
        title: t.Notifications.tableHeaders.statusFrequency,
        dataIndex: 'timings',
        key: 'timings',
        width: 250,
        render: props => (
          props.record.notificationType === NotificationType.APPROVE_STATUS && (
            <SelectStatusFrequency {...props} isEditingRecord={false} />
          )),
      }),
    ] as EditableColumnType<NotificationsRecord>[]).filter(Boolean),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, existedColumns, renderIfTypeExist]
  );

  return columns;
};
