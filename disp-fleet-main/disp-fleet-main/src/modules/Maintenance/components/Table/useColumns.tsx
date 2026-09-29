import React from 'react';

import { Select } from '@sber-sbertransport/ui-kit/src';

import { ColumnType } from 'antd/lib/table';
import moment from 'moment';

import { useMaintenanceTakeToWork, useMaintenanceRequestStatusUpdate } from 'api/maintenance/maintenance.api';
import { MaintenanceMonitorItem } from 'api/maintenance/maintenance.types';

import { DATE_FORMAT } from 'constants/app.constants';

import { ignore } from 'utils/utils';

import { Button } from 'components/Button';

import StatusLabel from './Status';

import { ReactComponent as Lightning } from 'assets/icons/lightning.svg';

import styles from './Table.module.scss';

import {
  MAINTENANCE_STATUSES, MaintenanceTabs, MaintenanceStatus, WashingStatus
} from 'api/maintenance/maintenance.constants';

const TERMINAL_STATUSES: string[] = [
  MaintenanceStatus.ISSUING_A_CAR,
  MaintenanceStatus.FINISHED,
  MaintenanceStatus.CANCELED,
  WashingStatus.CANCELLED,
];

export const useColumns = (type: MaintenanceTabs): { columns: ColumnType<MaintenanceMonitorItem>[] } => {
  const statusOptions = MAINTENANCE_STATUSES[type].map(({ value, label }) => ({ value, label }));

  const [takeToWork] = useMaintenanceTakeToWork(type);
  const [requestStatusUpdate] = useMaintenanceRequestStatusUpdate(type);

  const columns: ColumnType<MaintenanceMonitorItem>[] = [
    {
      title: '',
      dataIndex: 'takeToWorkTime',
      key: 'takeToWorkTime',
      width: 32,
      onCell: () => ({
        className: styles.noPaddingCell,
      }),
      render: (_value: number, record: MaintenanceMonitorItem) => {
        if (TERMINAL_STATUSES.includes(record.status) || record.takeToWorkTime) {
          return null;
        }

        return (
          <div className={styles.lightCell}>
            <Lightning />
          </div>
        );
      },
    },
    {
      title: 'ID Заявки',
      dataIndex: 'humanReadableId',
      key: 'humanReadableId',
      width: 165,
    },
    {
      title: 'Госномер',
      dataIndex: 'stateNumber',
      key: 'stateNumber',
      width: 115,
    },
    {
      title: 'Тип транспорта',
      dataIndex: 'type',
      key: 'type',
      width: 115,
    },
    {
      title: 'Дата создания заявки',
      dataIndex: 'creationTime',
      key: 'creationTime',
      width: 160,
      render: (value: number) => moment(value).format(DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA),
    },
    {
      title: 'Контрольный срок',
      dataIndex: 'deadlineTime',
      key: 'deadlineTime',
      width: 160,
      render: (value: number) => moment(value).format(DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA),
    },
    {
      title: 'Статус заявки',
      dataIndex: 'status',
      key: 'status',
      width: 255,
      render: (value: string, record: MaintenanceMonitorItem) => {
        if (TERMINAL_STATUSES.includes(value)) {
          return <StatusLabel status={value} type={type} />;
        }

        if (record.takeToWorkTime) {
          return (
            <Select
              value={value}
              options={statusOptions}
              size="small"
              onChange={newStatus => {
                requestStatusUpdate({ requestId: record.id, status: newStatus as string })
                  .catch(ignore);
              }}
            />
          );
        }

        return (
          <Button
            type="primary"
            size="small"
            onClick={() => takeToWork(record.id).catch(ignore)}
          >
            Взять работу
          </Button>
        );
      },
    },
    {
      title: 'Автор заявки',
      dataIndex: 'fullName',
      key: 'fullName',
    },
  ];

  return { columns };
};
