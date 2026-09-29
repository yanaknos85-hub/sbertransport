import { Table } from 'antd';
import * as React from 'react';
import { useOrganizations } from 'api/organizations';
import { Organization } from 'stores/Corporate/Corporate.interface';
import { Department, DepartmentDetailed } from 'stores/Department/Department.interface';
import { EmployeeStatus, EmployeeStatusTitle } from 'constants/constants.app';
import { useColumns } from './Components/Columns';
import { Footer } from './Components/Footer';

const department2row = (organizations: Record<string, Organization>, department: Department): DepartmentDetailed => {
  const departmentHead = () => `
      ${department.departmentHead?.lastName ?? ''} 
      ${department.departmentHead?.firstName ?? ''} 
      ${department.departmentHead?.patronymic ?? ''}
    `;

  const departmentStatusTitle
    = department.status === EmployeeStatus.ACTIVE ? EmployeeStatusTitle.ACTIVE : EmployeeStatusTitle.INACTIVE;

  return {
    humanReadableId: department.humanReadableId,
    departmentHead: departmentHead(),
    departmentHeadId: department.departmentHead?.id,
    parent: department.parent?.departmentName,
    location: department.location,
    easupId: department.easupId,
    fullStructurePath: department.fullStructurePath,
    id: department.id,
    organization: organizations[department.organizationId]?.officialName,
    organizationId: department.organizationId,
    code: department.code,
    departmentName: department.departmentName,
    children: department.children,
    employees: department.employees,
    status: department.status,
    statusTitle: departmentStatusTitle,
  };
};

export const DepartmentsTable: React.FC<{
  departments: Department[];
  isFetching: boolean;
}> = ({ departments, isFetching }) => {
  const columns = useColumns();

  const organizationsById = useOrganizations().data.byId;

  return (
    <Table
      loading={isFetching}
      bordered
      columns={columns}
      dataSource={departments.map(department => department2row(organizationsById, department))}
      rowKey="id"
      scroll={{ x: 3000 }}
      size="small"
      pagination={false}
      tableLayout="auto"
      footer={() => <Footer />}
    />
  );
};
