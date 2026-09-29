import * as React from 'react';
import { ColumnsType } from 'antd/lib/table';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import { Tag } from 'antd';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const columns: ColumnsType<any> = [
  {
    title: 'Id пользователя',
    dataIndex: 'humanReadableId',
    key: 'humanReadableId',
    fixed: 'left',
  },
  {
    title: 'Табельный номер',
    dataIndex: 'personnelNumber',
    key: 'personnelNumber',
  },
  {
    title: 'ФИО',
    dataIndex: 'nameWithInitials',
    key: 'nameWithInitials',
  },
  {
    title: 'Статус',
    dataIndex: 'statusTitle',
    key: 'statusTitle',
  },
  {
    title: 'Организация',
    dataIndex: 'organization',
    key: 'organization',
  },
  {
    title: 'Департамент',
    dataIndex: 'department',
    key: 'department',
  },
  {
    title: 'Позиция',
    dataIndex: 'position',
    key: 'position',
  },
  {
    title: 'Мобильный телефон',
    dataIndex: 'mobilePhone',
    key: 'mobilePhone',
  },
  {
    title: 'Email',
    dataIndex: 'email',
    key: 'mobilePhone',
  },
  {
    title: 'Руководитель',
    dataIndex: 'supervisor',
    key: 'supervisor',
  },
  {
    title: 'Признак делегата',
    dataIndex: 'delegatedBy',
    key: 'action',
  },
  {
    title: 'Делегирован',
    dataIndex: 'delegatedBy',
    key: 'delegatedBy',
  },
  {
    title: 'Доступные виды транспорта',
    dataIndex: 'availableTransportTypes',
    key: 'availableTransportTypes',
  },
  {
    title: 'Доступные классы автомобилей',
    dataIndex: 'availableTransportTypes',
    key: 'availableTransportTypes',
  },
  {
    title: 'Код подразделения',
    dataIndex: 'code',
    key: 'code',
  },
  {
    title: 'Признаки',
    dataIndex: 'attributes',
    key: 'attributes',
    // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
    render: (attributes: EmployeesAttribute[]) => attributes.map(({ id, name }) => <Tag key={id}>{name}</Tag>),
  },
];

export { columns };
