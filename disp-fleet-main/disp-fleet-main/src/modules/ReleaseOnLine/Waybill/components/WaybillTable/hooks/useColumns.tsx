import React, { useMemo } from 'react';

import { ColumnType } from 'antd/lib/table';
import moment from 'moment';
import { Link } from 'react-router-dom';

import { WaybillStatus } from 'api/waybill/waybill.constants';
import { WaybillSearchItem } from 'api/waybill/waybill.types';

import { DATE_FORMAT } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

import Status from '../components/Status';
import StatusBadge from '../components/StatusBadge';

export const useColumns = () => {
  const columns: ColumnType<WaybillSearchItem>[] = useMemo(() => [
    {
      title: 'ID заявки',
      dataIndex: 'humanReadableId',
      key: 'humanReadableId',
      width: 170,
      render: (id, record) => (
        <Link to={routes.WAYBILL_DETAILED.replace(':id', record.id)}>
          {id}
        </Link>
      ),
    },
    {
      title: 'Автопарк',
      dataIndex: 'organizationName',
      key: 'organizationName',
      width: 160,
    },
    {
      title: 'Филиал',
      // dataIndex: 'organizationName',
      key: 'branch',
      width: 160,
    },
    {
      title: 'Дата начала путевого листа',
      dataIndex: 'startDate',
      key: 'startDate',
      width: 155,
      render: (value: string) => moment(value).format(DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA),
    },
    {
      title: 'Дата окончания путевого листа',
      dataIndex: 'finishDate',
      key: 'finishDate',
      width: 155,
      render: (value: string) => moment(value).format(DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA),
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (value: WaybillStatus) => <Status status={value} />,
    },
    {
      title: 'Госномер',
      dataIndex: ['transport', 'stateNumber'],
      key: 'stateNumber',
      width: 110,
    },
    {
      title: 'Марка',
      dataIndex: ['transport', 'brand'],
      key: 'brand',
      width: 100,
    },
    {
      title: 'Модель',
      dataIndex: ['transport', 'model'],
      key: 'model',
      width: 102,
    },
    {
      title: 'ФИО водителя',
      dataIndex: 'driverFullName',
      key: 'driverFullName',
      width: 320,
    },
    {
      title: 'Прохождение медика',
      dataIndex: 'medicSuccess',
      key: 'medicSuccess',
      width: 148,
      render: (value: boolean) => <StatusBadge passed={value} />,
    },
    {
      title: 'Прохождение телемеханика',
      dataIndex: 'telemechSuccess',
      key: 'telemechSuccess',
      width: 187,
      render: (value: boolean) => <StatusBadge passed={value} />,
    },
  ], []);

  return { columns };
};
