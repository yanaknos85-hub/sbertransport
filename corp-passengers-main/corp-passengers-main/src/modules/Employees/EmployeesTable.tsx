import { Table, Tag } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import {
  EmployeeGender,
  EmployeeGenderTitle,
  EmployeeStatus,
  EmployeeStatusTitle
} from 'constants/constants.app';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Employee, IPersonalCar } from 'stores/Employee/Employee.interface';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import { nameWithInitials } from 'utils/employee';
import * as tt from 'utils/io-ts';

import { Footer } from './Components/Footer';

interface Row {
  id: tt.UUID;
  userId?: string;
  humanReadableId: string;
  personnelNumber: string;
  mobilePhone: string;
  email: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  availableTransportTypes: any[];
  attributes: EmployeesAttribute[];
  personalCars: IPersonalCar[];
  status: string;
  statusTitle: string;
  firstName: string;
  lastName: string;
  patronymic: string;
  gender: string;
  department: string;
  departmentId: tt.UUID;
  positionName: string;
  organization: string;
}

const employee2row = (employee: Employee): Row => ({
  id: employee.id,
  userId: employee?.userId || '',
  humanReadableId: employee.humanReadableId,
  personnelNumber: employee?.personnelNumber || '',
  mobilePhone: employee.mobilePhone || '',
  email: employee.email || '',
  availableTransportTypes: employee.availableTransportTypes || [],
  attributes: employee.attributes || [],
  personalCars: (employee.personalCars as IPersonalCar[]) || [],
  status: EmployeeStatus[employee.status || EmployeeStatus.ACTIVE],
  statusTitle: EmployeeStatusTitle[employee.status || EmployeeStatus.ACTIVE],
  firstName: employee.firstName,
  lastName: employee.lastName,
  gender: employee.gender || '',
  patronymic: employee.patronymic || '',
  department: employee.departmentName || '',
  departmentId: employee.departmentId,
  organization: employee.organizationName || '', // ToDo: разбираться почему не отображаются!
  positionName: employee.positionName || '',
});

const columns: ColumnsType<Row> = [
  {
    title: 'ID',
    dataIndex: 'humanReadableId',
    key: 'humanReadableId',
    fixed: 'left',
    render: (id, row) => (
      <Link to={`employees/${row.id}/departments/${row.departmentId}`}>
        {row.humanReadableId}
      </Link>
    ),
  },
  {
    title: 'ФИО',
    dataIndex: 'nameWithInitials',
    key: 'nameWithInitials',
    render: (id, row) => nameWithInitials(row),
  },
  {
    title: 'Табельный номер',
    dataIndex: 'personnelNumber',
    key: 'personnelNumber',
  },
  {
    title: 'Пол',
    dataIndex: 'gender',
    key: 'gender',
    render: (gender: EmployeeGender) => EmployeeGenderTitle[gender],
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
    title: 'Должность',
    dataIndex: 'positionName',
    key: 'positionName',
  },
  {
    title: 'Статус',
    dataIndex: 'status',
    key: 'status',
    render: (status: EmployeeStatus) => EmployeeStatusTitle[status],
  },
  {
    title: 'Email',
    dataIndex: 'email',
    key: 'mobilePhone',
  },
  {
    title: 'Доступные виды транспорта',
    dataIndex: 'availableTransportTypes',
    key: 'availableTransportTypes',
  },
  {
    title: 'Признаки',
    dataIndex: 'attributes',
    key: 'attributes',
    render: (attributes: EmployeesAttribute[]) => attributes && attributes.map(({ id, name }) => (
      <Tag key={id}>{name}</Tag>)
    ),
  },
];

export const EmployeesTable: React.FC<{
  employees: Employee[];
}> = ({ employees }) => (
  <Table
    bordered
    columns={columns}
    dataSource={employees.map(employee2row)}
    rowKey="id"
    scroll={{ x: 3000 }}
    size="small"
    pagination={false}
    tableLayout="auto"
    footer={() => <Footer />}
  />
);
