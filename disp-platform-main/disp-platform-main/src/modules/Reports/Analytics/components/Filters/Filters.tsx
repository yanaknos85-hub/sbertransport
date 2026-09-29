import React, { FC, useMemo, useState } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { DatePicker, Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import moment from 'moment';
import { useTranslation } from 'i18n';

import { AutoAnalyticsFilters } from 'api/analytics/analytics.types';
import { analyticsTransportType, AnalyticsTransportType, subtypesText } from 'api/analytics/analytics.constants';
import { useAnalyticsTransport } from 'api/analytics/analytics.api';
import { Button } from 'components/Button';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { useRoleMap } from 'hooks/useRoleMap';
import { monthNames } from 'utils/calendar';

import { useAnalyticsQuery } from '../../context/AnalyticsQuery';
import { disabledYears, formatFormValue } from './utils';

import { ReactComponent as CalendarSVG } from 'assets/icons/calendar.svg';
import styles from './Filters.module.scss';

export interface FormValues {
  year: number;
  contractorIds?: string;
  autoparkIds?: string | string[];
  months?: number[];
  vehicles?: string[];
}

const monthOptions = monthNames.map((month, idx) => ({ value: idx + 1, label: month }));

export const Filters: FC = () => {
  const t = useTranslation().t.Analytics;
  const { isAdmin, isManager } = useRoleMap();

  const { query, setQuery } = useAnalyticsQuery();

  const [form] = useForm();

  const defaultValues = useMemo(
    () => ({
      contractorIds: query.contractorIds ? (isAdmin ? query.contractorIds : query.contractorIds[0]) : undefined,
      autoparkIds: query.autoparkIds ? (isAdmin || isManager ? query.autoparkIds : query.autoparkIds[0]) : undefined,
      year: moment(query.year, 'YYYY'),
      months: query.months,
      type: AnalyticsTransportType.BRAND_NAME,
    }),
    [query, isAdmin, isManager]
  );

  const [type, setType] = useState(defaultValues.type);
  const [contractorIds, setContractorIds] = useState(defaultValues.contractorIds);
  const [autoparkIds, setAutoparkIds] = useState(defaultValues.autoparkIds);

  const onFinish = (values: FormValues) => {
    const isBrandsFilter = type === AnalyticsTransportType.BRAND_NAME && !!values.vehicles?.length;
    const isStateNumbersFilter = type === AnalyticsTransportType.STATE_NUMBER && !!values.vehicles?.length;

    setQuery({
      contractorIds: isAdmin ? formatFormValue(values.contractorIds) : query.contractorIds,
      autoparkIds: isAdmin || isManager ? formatFormValue(values.autoparkIds) : query.autoparkIds,
      year: moment(values.year).year(),
      months: values.months?.length ? values.months : undefined,
      brands: isBrandsFilter ? values.vehicles : undefined,
      stateNumbers: isStateNumbersFilter ? values.vehicles : undefined,
    } as AutoAnalyticsFilters);
  };

  const onValuesChange = (changedValues: FormValues) => {
    if ('contractorIds' in changedValues) {
      form.setFieldsValue({ autoparkIds: undefined, vehicles: undefined });
    }
    if ('autoparkIds' in changedValues) {
      form.setFieldsValue({ vehicles: undefined });
    }
    if ('type' in changedValues) {
      form.setFieldsValue({ vehicles: undefined });
    }
  };

  const { data: transport, isFetching: isPendingTransport } = useAnalyticsTransport({
    data: type,
    contractorIds: formatFormValue(contractorIds),
    autoparkIds: formatFormValue(autoparkIds),
  }, {
    suspense: false,
  });

  return (
    <div className={styles.container}>
      <Form
        form={form}
        className={styles.form}
        initialValues={defaultValues}
        onValuesChange={onValuesChange}
        onFinish={onFinish}
      >
        <div className={styles.wrapper}>
          {(isAdmin || isManager) && (
            <Form.Item name="contractorIds">
              <AutoparkSelect
                mode={isAdmin ? 'multiple' : undefined}
                placeholder={t.fields.autoparkId}
                allowClear
                disabled={!isAdmin}
                onChange={setContractorIds}
                isInternal
              />
            </Form.Item>
          )}
          <Form.Item dependencies={['contractorIds']} noStyle>
            {({ getFieldValue }) => {
              const contractorIds = getFieldValue('contractorIds');
              const isString = typeof contractorIds === 'string';
              const orgId = isString ? contractorIds : contractorIds?.[0];

              return (
                <Form.Item name="autoparkIds" className={styles.growSelect}>
                  <BranchSelect
                    mode={isAdmin || isManager ? 'multiple' : undefined}
                    autoparkId={orgId}
                    placeholder={t.fields.branches}
                    allowClear
                    disabled={!(isAdmin || isManager) || (isAdmin && contractorIds?.length > 1)}
                    onChange={setAutoparkIds}
                  />
                </Form.Item>
              );
            }}
          </Form.Item>
        </div>

        <div className={styles.wrapper}>
          <Form.Item name="type">
            <Select
              size="small"
              showDivider
              showSearch={false}
              options={Object.entries(analyticsTransportType).map(([value, label]) => ({ value, label }))}
              onChange={setType}
            />
          </Form.Item>
          <Form.Item
            name="vehicles"
            className={styles.growSelect}
          >
            <Select
              mode="multiple"
              maxTagCount={1}
              size="small"
              showDivider
              allowClear
              showSearch={false}
              options={transport?.map(value => ({ value })) ?? []}
              loading={isPendingTransport}
              placeholder={subtypesText[type]}
            />
          </Form.Item>
          <Form.Item name="year">
            <DatePicker
              picker="year"
              allowClear={false}
              disabledDate={disabledYears}
              suffixIcon={<CalendarSVG />}
            />
          </Form.Item>
          <Form.Item name="months">
            <Select
              mode="multiple"
              maxTagCount={1}
              placeholder="Весь год"
              size="small"
              showDivider
              allowClear
              showSearch={false}
              className={styles.months}
              options={monthOptions}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            Обновить
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default Filters;
