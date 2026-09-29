import {
  Button, Form, Input, Select
} from 'antd';
import { LabeledValue } from 'antd/lib/select';
import { useAllChildrenDepartments, useDepartments, useUpdateDepartment } from 'api/departments';
import { useEmployees } from 'api/employee';
// import { useCurrentLimit, useSharing, useSharingPercentages } from 'api/limits';
import { useProfile } from 'api/profile';
import { useGetListRegions } from 'api/tariffs';
import * as React from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Department } from 'stores/Department/Department.interface';
import { nameWithInitials } from 'utils/employee';
import { UUID } from 'utils/io-ts';
import { searchSymbol } from 'utils/searchSymbol';
import { useEffect, useState } from 'react';
import { Limit } from 'stores/Limits/Models/Limit';
import { useSearchLimits } from 'api/limits/search';
import { EmployeeStatus } from 'constants/constants.app';

interface Fields {
  code: string;
  departmentName: string;
  parentId: UUID;
  departmentHeadId: UUID;
  geozoneId?: UUID;
  humanReadableId: string;
  easupId?: string;
}

const dept2fields = ({
  code,
  departmentName,
  parent,
  departmentHead,
  geozoneId,
  humanReadableId,
  easupId,
}: Department): Fields => ({
  code,
  departmentName,
  parentId: parent?.id as UUID,
  departmentHeadId: departmentHead?.id as UUID,
  geozoneId,
  humanReadableId: humanReadableId || '',
  easupId,
});

const updated = (
  dept: Department,
  {
    code, departmentName, parentId, departmentHeadId, geozoneId, humanReadableId, easupId,
  }: Fields
): Department => ({
  ...dept,
  code,
  departmentName,
  parent: { id: parentId },
  departmentHead: { id: departmentHeadId },
  geozoneId,
  humanReadableId,
  easupId,
});

interface Params { id: string }

const GenSelectDepartment: React.FC<{
  limitId: UUID;
  organizationId: UUID;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  selectProps: any;
}> = ({
  limitId, organizationId, selectProps,
}): JSX.Element => {
  const { refetch, data } = useAllChildrenDepartments(organizationId, limitId);

  const departments = data?.departmentsResponse?.content;

  React.useEffect(() => {
    refetch();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId]);

  const getSelectElement = (array: Department[]): JSX.Element => {
    const activeDepsArray = array.filter(({ status }) => status === EmployeeStatus.ACTIVE);

    return (
      <Select {...selectProps}>
        {activeDepsArray?.map(({ id, departmentName }) => (
          <Select.Option key={id} value={id}>
            {departmentName}
          </Select.Option>
        ))}
      </Select>
    );
  };

  return getSelectElement(departments);
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const SelectDepartment: React.FC<React.ComponentProps<typeof Select> & { parentdep?: any }> = selectProps => {
  const { organizationId } = useProfile().data;
  const {
    params: { id },
  } = useRouteMatch<Params>();

  // @ts-ignore
  const [searchLimit] = useSearchLimits(organizationId, { humanReadableLimitId: id as UUID });

  const [limit, setLimit] = useState<Limit>();

  const getLimit = async (): Promise<void> => {
    const res = await searchLimit();

    if (res) {
      setLimit(res.content[0]);
    }
  };

  useEffect(() => {
    getLimit().then();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return limit ? (
    <GenSelectDepartment
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      limitId={(limit as any).department.id}
      // @ts-ignore
      organizationId={organizationId}
      selectProps={selectProps}
    />
  ) : null;
};

export const SelectEmployee: React.FC<
  React.ComponentProps<typeof Select> & {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    parentdep?: any;
  }
> = props => {
  const profile = useProfile().data!;
  const { organizationId } = profile!;

  // @ts-ignore
  const { content: employees } = useEmployees(organizationId).data.employeeResponse;

  const employeesFromCurrentDepartment = props.parentdep
    ? employees.filter(x => x.departmentId === props.parentdep)
    : [];

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const getSelectElement = (array: any[]): JSX.Element => (
    <Select {...props}>
      {array?.map(({ id, ...employee }) => (
        <Select.Option key={id} value={id}>
          {nameWithInitials(employee)}
        </Select.Option>
      ))}
    </Select>
  );

  return props.parentdep ? getSelectElement(employeesFromCurrentDepartment) : getSelectElement(employees);
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type FormItem<F extends Record<string, any>> = React.FC<React.ComponentProps<typeof Form.Item> & { name: keyof F }>;

const Field: FormItem<Fields> = Form.Item;

const EditDepartment: React.FC = () => {
  const {
    params: { id: departmentId },
  } = useRouteMatch<{ id: UUID }>();

  const { organizationId } = useProfile().data;

  // @ts-ignore
  const { byId: departmentsById } = useDepartments(organizationId).data;

  const department: Department = departmentsById[departmentId]!;

  const geoZonesList = useGetListRegions().data;
  const geoZonesOptions: LabeledValue[] = geoZonesList?.map(({ name, id }) => ({ label: name, value: id }));

  const initialValues = React.useMemo(() => dept2fields(department), [department]);

  const [update] = useUpdateDepartment();

  const { logger } = useAppStoreContext();

  const onFinish = (values: Fields): Promise<void> => (
    update({ orgId: organizationId, department: updated(department, values) }).then(() => {
      logger.toMessage('success', 'Изменения сохранены');
    })
  );

  const onFinishFailed = () => {
    logger.toNotify('error',
      'Ошибка',
      'Изменения не сохранены',
      5,
      667
    );
  };

  return (
    <div style={{ minWidth: '600px', marginTop: '32px' }}>
      <Form
        initialValues={initialValues}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        labelAlign="right"
        labelCol={{ span: 8 }}
        wrapperCol={{ span: 16 }}
        size="large"
      >
        <Field
          label="Название"
          name="departmentName"
          rules={[ValidationRules.general.required]}
        >
          <Input />
        </Field>

        <Field label="Код" name="code">
          <Input />
        </Field>

        <Field label="ID" name="humanReadableId">
          <Input />
        </Field>

        <Field label="Местоположение" name="geozoneId">
          <Select
            options={geoZonesOptions}
            showSearch
            optionFilterProp="label"
            filterOption={searchSymbol}
          />
        </Field>

        <Field label="Родительское подразделение" name="parentId">
          <SelectDepartment />
        </Field>

        <Field label="Глава подразделения" name="departmentHeadId">
          <SelectEmployee />
        </Field>

        <Field label="ID ЕАСУП" name="easupId">
          <Input disabled />
        </Field>

        <div style={{
          marginTop: '32px', display: 'flex', justifyContent: 'center',
        }}
        >
          <Form.Item>
            <Button type="primary" htmlType="submit">
              Сохранить
            </Button>
          </Form.Item>
        </div>
      </Form>
    </div>
  );
};

export default EditDepartment;
