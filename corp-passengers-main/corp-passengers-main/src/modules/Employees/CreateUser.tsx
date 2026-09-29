import { RollbackOutlined, SaveOutlined } from '@ant-design/icons';
import {
  Button, Form, Input, Popconfirm, Select
} from 'antd';
import { useCreateEmployee } from 'api/employee';
import { useEmployeeAttributes } from 'api/employee-attributes';
import { useProfile } from 'api/profile';
import * as R from 'ramda';
import * as React from 'react';
import { useHistory } from 'react-router-dom';
import AccessControl from 'shared/components/AccessControl';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import { SelectRole } from 'shared/components/SelectRole';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import { Role } from 'stores/Roles/Roles.interface';
import styled from 'styled-components';
import { preventDefault, settingsPhoneNumber } from 'utils';
import { UUID } from 'utils/io-ts';
import { useRole } from 'utils/useRole';

import { SelectDepartment } from 'shared/components/SelectDepartment';
import { SelectPosition } from './EditEmployee';

interface SelectOption {
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

interface FormFields {
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber: string;
  departmentId: UUID;
  positionId: UUID;
  roles: Role['code'][];
  mobilePhone: string;
  email: string;
  attributes: UUID[];
}

export const sleep = (n: number): Promise<void> => new Promise(resolve => setTimeout(resolve, n));

// eslint-disable-next-line @typescript-eslint/no-unused-vars, @stylistic/comma-dangle
const retry = (timeout: number) => (retries: number) => <T,>(f: () => Promise<T>): Promise<T> => (
  retries === 0 ? f() : f().catch(() => sleep(timeout).then(() => retry(timeout)(retries - 1)(f)))
);

const CreateEmployee: React.FC = () => {
  const userRoles = useRole();
  const profile = useProfile().data;
  const [createEmployee] = useCreateEmployee();

  const history = useHistory();
  // @ts-ignore
  const attributes = useEmployeeAttributes(profile.organizationId).data;
  const { logger } = useAppStoreContext();

  const attributeOptions: SelectOption[] = attributes
    .filter(R.whereEq({ status: 'ACTIVE' }))
    .map(({ id, name }) => ({ value: id, label: name }));

  const [busy, setBusy] = React.useState(false);

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
    attributes: newAttributes = [],
  }: FormFields) => {
    setBusy(true);
    createEmployee({
      status: 'ACTIVE',
      organizationId: profile.organizationId,
      personnelNumber,
      firstName,
      lastName,
      patronymic,
      departmentId,
      positionId,
      roles,
      email,
      mobilePhone: settingsPhoneNumber.clearPhone(mobilePhone),
      attributes: R.innerJoin<EmployeesAttribute, UUID>((record, id) => record.id === id)(
        attributes,
        newAttributes
      ),
    })
      .then(() => {
        logger.toMessage('success', 'Запись сохранена');
        history.goBack();
      })
      .catch(err => {
        if (err.response.status === 409) {
          logger.toMessage(
            'error',
            `Сотрудник с табельным номером ${err.response.data.problems[0].value} уже существует`
          );
        }
        // eslint-disable-next-line no-console
        console.error('Error creating employee', err);
      })
      .finally(() => setBusy(false));
  };

  const goBack = React.useCallback(() => sleep(200).then(() => history.push('/directories/employees')), [history]);

  return (
    <div style={{ position: 'relative', filter: busy ? 'grayscale(0.75)' : 'none' }}>
      <Form onFinish={handleSubmit}>
        <TableBody>
          <Form.Item
            name="firstName"
            label="Имя"
            rules={[ValidationRules.general.required]}
          >
            <Input onPressEnter={preventDefault} />
          </Form.Item>
          <Form.Item
            name="lastName"
            label="Фамилия"
            rules={[ValidationRules.general.required]}
          >
            <Input onPressEnter={preventDefault} />
          </Form.Item>
          <Form.Item name="patronymic" label="Отчество">
            <Input onPressEnter={preventDefault} />
          </Form.Item>
          <Form.Item
            name="personnelNumber"
            label="Табельный номер"
            rules={[ValidationRules.general.required, ValidationRules.general.onlyDigits()]}
          >
            <Input onPressEnter={preventDefault} />
          </Form.Item>
          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={['ROLE_ADMIN_CORP_CLIENT', 'ROLE_INITIAL_USER', 'ROLE_ACCESS_ADMIN']}
            renderNoAccess={() => null}
          >
            <Form.Item
              name="roles"
              label="Роли"
              rules={[ValidationRules.general.required]}
            >
              <SelectRole
                mode="multiple"
                optionLabelProp="label"
                onInputKeyDown={preventDefault}
              />
            </Form.Item>
          </AccessControl>

          <Form.Item
            name="departmentId"
            label="Подразделение"
            rules={[ValidationRules.general.required]}
          >
            <SelectDepartment onInputKeyDown={preventDefault} />
          </Form.Item>
          <Form.Item
            name="positionId"
            label="Должность"
            rules={[ValidationRules.general.required]}
          >
            <SelectPosition filter={({ active = false }) => active} onInputKeyDown={preventDefault} />
          </Form.Item>
          <Form.Item name="mobilePhone" label="Мобильный телефон">
            <CustomInput {...settingsPhoneNumber.input} onPressEnter={preventDefault} />
          </Form.Item>
          <Form.Item
            name="email"
            label="e-mail"
            rules={[ValidationRules.general.required]}
          >
            <Input onPressEnter={preventDefault} />
          </Form.Item>
          <Form.Item name="attributes" label="Признаки сотрудника">
            <Select
              showSearch
              mode="multiple"
              optionFilterProp="label"
              options={attributeOptions}
              onInputKeyDown={preventDefault}
            />
          </Form.Item>
        </TableBody>

        <FormButtons>
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
      {busy ? (
        <div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'transparent',
            cursor: 'progress',
          }}
        />
      ) : null}
    </div>
  );
};

export default CreateEmployee;
