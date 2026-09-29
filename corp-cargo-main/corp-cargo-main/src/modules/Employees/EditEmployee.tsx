import { RollbackOutlined, SaveOutlined } from '@ant-design/icons';
import {
  Button, Form, Input, Popconfirm, Select
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useEmployee, useUpdateEmployee } from 'api/employee';
import { useEmployeeAttributes } from 'api/employee-attributes';
import { usePositions } from 'api/positions';
import { useProfile } from 'api/profile';
import { useEmployeeRoles } from 'api/roles';
import * as R from 'ramda';
import * as React from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import AccessControl from 'shared/components/AccessControl';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import { SelectRole } from 'shared/components/SelectRole';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Employee } from 'stores/Employee/Employee.interface';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import { Position } from 'stores/Position/Position.interface';
import { Role } from 'stores/Roles/Roles.interface';
import styled from 'styled-components';
import { settingsPhoneNumber } from 'utils';
import { UUID } from 'utils/io-ts';
import { useRole } from 'utils/useRole';

import { SelectDepartment } from 'shared/components/SelectDepartment';
import { ConfirmModal } from './Components/Modals/ConfirmModal';
import { sleep } from './CreateUser';

export interface SelectOption {
  value: string;
  label: string;
}

const FormButtons = styled.div`
  margin: 32px auto;
  display: flex;
  flex-direction: row;
  justify-content: center;

  & > * + * {
    margin-left: 16px;
  }
`;

const TableBody = styled.div`
  display: table;
  border-spacing: 10px;
  border-collapse: separate;

  .ant-row {
    display: table-row;
  }

  .ant-col {
    display: table-cell;
  }
`;

export const SelectPosition = ({
  filter = R.always(true),
  ...props
}: React.ComponentProps<typeof Select> & { filter?: (pos: Position) => boolean }): JSX.Element => {
  const profile = useProfile().data;
  // @ts-ignore
  const { positions } = usePositions(profile.organizationId ?? profile.organizationId).data;

  const options = positions.filter(filter).map(({ id, positionName }) => ({
    value: id,
    label: positionName,
  }));

  return (
    <Select
      options={options}
      {...props}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

interface FormFields {
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber?: string;
  departmentId: UUID;
  positionId: UUID;
  roles: Role['code'][];
  mobilePhone: string;
  email: string;
  attributes: UUID[];
  status: 'ACTIVE' | 'INACTIVE';
}

const employee2fields = ({
  firstName,
  lastName,
  patronymic = '',
  personnelNumber,
  departmentId,
  positionId,
  mobilePhone = '',
  email = '',
  attributes = [],
  status,
}: Employee): FormFields => ({
  firstName,
  lastName,
  patronymic,
  personnelNumber,
  departmentId,
  positionId,
  mobilePhone,
  email,
  status,
  attributes: attributes.map(R.prop('id')),
  roles: [],
});

const statusOptions = [
  { label: 'Активен', value: 'ACTIVE' },
  { label: 'Неактивен', value: 'INACTIVE' },
];

const SelectStatus: React.ComponentType<React.ComponentProps<typeof Select>> = props => (
  <Select options={statusOptions} {...props} />
);

const EditEmployeeForm: React.FC<{ employee: Employee }> = ({ employee }): JSX.Element => {
  const employeeRoles: Role['code'][] = useEmployeeRoles(employee?.userId ?? '', employee?.orgStructureType).data;
  const [form] = useForm();

  React.useEffect(() => {
    form.setFieldsValue({ roles: employeeRoles });
  }, [employeeRoles, form]);

  const profile = useProfile().data;
  const [updateEmployee] = useUpdateEmployee();

  const history = useHistory();
  // @ts-ignore
  const attributes = useEmployeeAttributes(profile.organizationId).data;

  const attributeOptions: SelectOption[] = attributes
    .filter(R.whereEq({ status: 'ACTIVE' }))
    .map(({ id, name }) => ({ value: id, label: name }));

  const { logger } = useAppStoreContext();

  const handleSubmit = ({
    firstName,
    lastName,
    patronymic,
    personnelNumber,
    departmentId,
    positionId,
    roles,
    mobilePhone,
    email,
    status,
    attributes: newAttributes,
  }: FormFields) => {
    const {
      id, userId, organizationId, humanReadableId, departmentId: originalDepartmentId,
    } = employee;

    const updatedEmployee = {
      id,
      userId,
      organizationId,
      humanReadableId,
      status,
      firstName,
      lastName,
      patronymic,
      personnelNumber,
      departmentId,
      positionId,
      roles,
      mobilePhone: settingsPhoneNumber.clearPhone(mobilePhone),
      email,
      attributes: R.innerJoin<EmployeesAttribute, UUID>((record, id) => record.id === id)(attributes, newAttributes),
    };

    return updateEmployee({ departmentId: originalDepartmentId, updatedEmployee })
      .then(R.tap(() => logger.toMessage('success', 'Запись сохранена')))
      .catch(() => logger.toMessage('error', 'Ошибка сохранения записи'));
  };

  const goBack = React.useCallback(() => sleep(200).then(() => history.push('/directories/employees')), [history]);
  const userRoles = useRole();
  return (
    <div>
      <Form
        onFinish={handleSubmit as any}
        initialValues={employee2fields(employee)}
        form={form}
      >
        <TableBody>
          <Form.Item label="ID">
            <span>{employee.humanReadableId}</span>
          </Form.Item>
          <Form.Item name="status" label="Статус">
            <SelectStatus />
          </Form.Item>
          <Form.Item
            name="firstName"
            label="Имя"
            required
            rules={[ValidationRules.general.required]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="lastName"
            label="Фамилия"
            required
            rules={[ValidationRules.general.required]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="patronymic" label="Отчество">
            <Input />
          </Form.Item>
          <Form.Item
            name="personnelNumber"
            label="Табельный номер"
            required
            rules={[ValidationRules.general.required, ValidationRules.general.onlyDigits()]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="email"
            label="e-mail"
            required
            rules={[ValidationRules.general.required]}
          >
            <Input />
          </Form.Item>

          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={['ROLE_ADMIN_CORP_CLIENT', 'ROLE_INITIAL_USER', 'ROLE_ACCESS_ADMIN']}
            renderNoAccess={() => null}
          >
            <Form.Item
              name="roles"
              label="Роли"
              required
              rules={[ValidationRules.general.required]}
            >
              <SelectRole mode="multiple" optionLabelProp="label" />
            </Form.Item>
          </AccessControl>

          <Form.Item
            name="departmentId"
            label="Подразделение"
            required
            rules={[ValidationRules.general.required]}
          >
            <SelectDepartment />
          </Form.Item>
          <Form.Item
            name="positionId"
            label="Должность"
            required
            rules={[ValidationRules.general.required]}
          >
            <SelectPosition filter={({ active = false }) => active} />
          </Form.Item>
          <Form.Item name="mobilePhone" label="Мобильный телефон">
            <CustomInput {...settingsPhoneNumber.input} />
          </Form.Item>
          <Form.Item name="attributes" label="Признаки сотрудника">
            <Select
              showSearch
              mode="multiple"
              options={attributeOptions}
            />
          </Form.Item>
        </TableBody>

        <FormButtons>
          <AccessControl userPermissions={userRoles} allowedPermissions={['ROLE_ADMIN_CORP_CLIENT']}>
            <ConfirmModal employee={employee} />
          </AccessControl>

          <Form.Item>
            <Button
              icon={<SaveOutlined />}
              size="middle"
              htmlType="submit"
            >
              Сохранить
            </Button>
          </Form.Item>
          <Popconfirm
            placement="top"
            title="Отменить?"
            onConfirm={goBack}
            okText="OK"
            cancelText="Не отменять"
            style={{ width: 300 }}
          >
            <Button icon={<RollbackOutlined />} size="middle">
              Отменить
            </Button>
          </Popconfirm>
        </FormButtons>
      </Form>
    </div>
  );
};

const EditEmployee: React.FC = () => {
  const urlParams = new URL(window.location.href).pathname.split('/');
  const { organizationId } = useProfile().data;
  const employeeId = urlParams[5] as UUID;
  const departmentId = urlParams[7] as UUID;

  // @ts-ignore
  const employee = useEmployee(organizationId, departmentId, employeeId).data;
  return employee ? <EditEmployeeForm employee={employee} /> : (
    <h1>
      Employee with ID
      {employeeId}
      {' '}
      not found
    </h1>
  );
};

export default EditEmployee;
