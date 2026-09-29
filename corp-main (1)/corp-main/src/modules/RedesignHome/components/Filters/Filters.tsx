import React, { useEffect, useState } from 'react';
import {
  Button, Form, Select
} from 'antd';
import moment from 'moment';
import { observer } from 'mobx-react';
import { SelectValue } from 'antd/lib/select';
import { StoreNames } from 'stores';
import { FormInstance } from 'antd/lib/form/Form';
import { UUID } from 'utils/io-ts';

import ChevronSmall from 'shared/images/menu 2.0/ChevronSmall';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { IMetricsMainData } from 'modules/RedesignHome/types/Home.types';
import { OrganizationsGroup } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { SelectOrganizationMetrics } from 'shared/components/SelectOrganizationMetrics';
import DepartmentField from 'shared/components/DepartmentField';
import { SelectExecutorGroupMetrics } from 'shared/components/SelectExecutorGroupMetrics';
import { ExecutorGroups } from 'api/executor-group';
import { useOrganizationContext } from 'context/Organization.context';

import styles from './Filters.module.scss';
import './override.scss';

export const DEFAULT_DATE = {
  mode: 'range',
  value: [moment().startOf('year'), moment().endOf('day')],
};

export const getDisabledDate = (value: moment.Moment): boolean => {
  const now = moment().add(-1, 'days');
  return value < now || value > now.add(1, 'years');
};

export const Filters = observer(({
  form,
  orgGroups,
  executorGroup,
}: {
  form: FormInstance;
  orgGroups: OrganizationsGroup[];
  executorGroup: ExecutorGroups[];
}) => {
  const { [StoreNames.homeStore]: homeStore } = useAppStoreContext();
  const [isOrganization, setIsOrganization] = useState<SelectValue>('organization');
  const [organizationsValue, setOrganizationsValue] = useState<string[]>();
  const [serviceTypes, setServiceTypes] = useState(['ALL']);
  const { Option } = Select;
  const [orgGroup, setOrgGroup] = useState<UUID | undefined>();
  const options = [{
    value: 'executorGroup',
    label: 'По группам исполнителей',
  },
  {
    value: 'organization',
    label: 'По организации',
  }];
  const optionsServiceTypes = [
    {
      value: 'ALL',
      label: 'Все сервисы',
    },
    {
      value: 'PASSENGER',
      label: 'Пассажирские перевозки',
    },
    {
      value: 'AUTOSERVICE',
      label: 'Автосервис',
    },
    {
      value: 'CARGO',
      label: 'Грузовые перевозки',
    },
  ];
  const { organizationId, organizationName } = useOrganizationContext();
  const [defaultOrgOption] = useState(() => organizationId && organizationName ? {
    value: organizationId,
    label: organizationName,
  } : undefined);

  const onFinish = values => {
    const data = {
      startDate: moment(values.date.value[0]).startOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]'),
      finishDate: values.date.value[1]
        ? moment(values.date.value[1]).endOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]')
        : moment(values.date.value[0]).endOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]'),
      serviceTypes: values.serviceTypes.indexOf('ALL') !== -1 ? ['PASSENGER', 'AUTOSERVICE', 'CARGO'] : values.serviceTypes,
      organizationIds: values.isOrganization === 'organization' && values.organizationIds?.length ? values.organizationIds : undefined,
      executorGroupIds: values.isOrganization === 'executorGroup' && values.organizationIds?.length ? values.organizationIds : undefined,
      departmentIds: values?.departmentIds?.length ? values.departmentIds : undefined,
    };

    homeStore.getMetrics(data as IMetricsMainData).then(el => homeStore.setMetricsData(el));
  };

  useEffect(() => {
    if (organizationId) {
      form.setFieldsValue({ organizationIds: isOrganization === 'organization' ? [organizationId] : undefined });
      setOrganizationsValue(isOrganization === 'organization' ? [organizationId] : []);
    }
  }, [isOrganization]);

  const onClearDepartments = () => {
    form?.setFieldsValue({
      departmentIds: undefined,
    });
  };

  const handleChangeOrganizations = e => {
    setOrganizationsValue(e);
    onClearDepartments && onClearDepartments();
  };

  const disabledSelectDepartment = !organizationsValue || (organizationsValue
    && (organizationsValue.length < 1 || organizationsValue.length > 1 || !organizationsValue));

  const handleServiceTypes = e => {
    const changedServiceTypes = (e.indexOf('ALL') !== -1 && e.length <= 2 && e.lastIndexOf('ALL') === 0)
      ? e.filter(el => el !== 'ALL') : (e.indexOf('ALL') !== -1 && e.length > 2)
        ? ['ALL'] : (e.indexOf('ALL') !== -1
        && e.length <= 2 && e.lastIndexOf('ALL') !== 0)
          ? ['ALL'] : e;

    setServiceTypes(e.length < 1 ? ['ALL'] : changedServiceTypes);
  };

  useEffect(() => {
    form.setFieldsValue({ serviceTypes: serviceTypes });
  }, [serviceTypes]);

  const onGroupChange = (id: unknown) => {
    setOrgGroup(id as UUID);
    form.setFieldsValue({ corpClient: undefined });
  };

  const onOrgListChange = (list: Organization[]) => {
    form.setFieldsValue({ organizationIds: orgGroup ? list.map(({ id }) => id) : organizationsValue });
    orgGroup && setOrganizationsValue(list.map(({ id }) => id));
  };

  const disabledDate = current => {
    return current && current < moment('2025-01-01').startOf('day');
  };

  useEffect(() => {
    const date = form.getFieldValue('date');

    // Если при загрузке фильтра поле даты не заполнено, заполняем его дефолтными значениями
    if (!date.value[0]) {
      form.setFieldsValue({ date: DEFAULT_DATE });
    }
  }, []);

  return (
    <div className="home_wrapper_main">
      <Form
        layout="vertical"
        form={form}
        name="create-request"
        size="middle"
        onFinish={onFinish}
      >
        <div className={styles.wrapper_form_filters}>
          <div>
            <Form.Item
              key="date"
              name="date"
              style={{ width: '25%', margin: '0 12px 0 0' }}
              rules={[{
                validator: (_, v) => v.value[0] ? Promise.resolve()
                  : Promise.reject(new Error('Укажите дату')),
              }]}
              initialValue={DEFAULT_DATE}
            >
              <NewDateInput
                withAvialableFuture
                yearsDisabled
                placeholder="Выберите дату"
                disabledDateRange={disabledDate}
              />
            </Form.Item>
            <Form.Item
              key="isOrganization"
              name="isOrganization"
              style={{ width: '25%', margin: '0 12px 0 0' }}
              initialValue="organization"
            >
              <Select
                showArrow
                suffixIcon={(
                  <div style={{ borderLeft: '1px solod grey' }}>
                    <ChevronSmall />
                  </div>
                )}
                options={options}
                placeholder="По организации"
                onChange={setIsOrganization}
              />
            </Form.Item>
            {isOrganization === 'organization'
            && (
            <Form.Item style={{ width: '25%', margin: '0 12px 0 0' }}>
              <Select
                allowClear
                value={orgGroup}
                onChange={onGroupChange}
                placeholder="Группа организаций"
              >
                {orgGroups.map(group => (
                  <Option key={group.id} value={group.id}>
                    {group.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
            )}
            <Form.Item
              key="organizationIds"
              name="organizationIds"
              style={{ width: isOrganization === 'organization' ? '25%' : '50%' }}
            >
              {isOrganization === 'organization'
                ? (
                  <SelectOrganizationMetrics
                    suffixIcon={<ChevronSmall />}
                    onChange={handleChangeOrganizations}
                    groupId={orgGroup}
                    defaultOption={defaultOrgOption}
                    onListChange={onOrgListChange}
                  />
                )
                : (
                  <SelectExecutorGroupMetrics
                    suffixIcon={<ChevronSmall />}
                    isHome
                    executorGroup={executorGroup}
                  />
                )}
            </Form.Item>
          </div>
          <div>
            <DepartmentField
              filterOrganizationId={organizationsValue}
              disabledfilterOrganizationId={disabledSelectDepartment}
            />
            <Form.Item
              key="serviceTypes"
              name="serviceTypes"
              style={{ width: '25%', margin: '0 12px 0 0' }}
              initialValue={['ALL']}
            >
              <Select
                mode="multiple"
                showArrow
                suffixIcon={(
                  <div style={{ borderLeft: '1px solod grey' }}>
                    <ChevronSmall />
                  </div>
                )}
                options={optionsServiceTypes}
                placeholder="Все сервисы"
                maxTagCount="responsive"
                onChange={handleServiceTypes}
                value={serviceTypes}
              />
            </Form.Item>
            <Button htmlType="submit" className={styles.button_reload}>Обновить</Button>
          </div>
        </div>
      </Form>
    </div>
  );
});
