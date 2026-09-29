import React, {
  useEffect, useMemo, FocusEvent, useState
} from 'react';
import {
  Form, Input, Spin
} from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { Translation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { TransportStatus, StatusesNames, TransportSearchRequest } from 'api/transport/transport.types';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { useBrandsSelectOptions } from 'modules/Vehicles/hooks/useBrandsSelectOptions';
import { useModelsSelectOptions } from 'modules/Vehicles/hooks/useModelsSelectOptions';
import { useRoleMap } from 'hooks/useRoleMap';
import { getEmptyFieldValues } from 'utils/form';

import styles from './formJSX.module.scss';

type FormValues = Omit<TransportSearchRequest, 'page' | 'searchText' | 'status'> & {
  status?: string;
};

const statusOptions = Object.values(TransportStatus).map(value => ({
  value,
  label: StatusesNames[value],
}));

export const transportFormJSX
  = (t: Translation) => (
    form: FormInstance,
    initialValues: Record<string, unknown>,
    onFinish: (values: Record<string, unknown>) => void
  ): JSX.Element => {
    const { contractorId, autoparkId } = useProfile().data;

    const { isAdmin, isManager } = useRoleMap();

    const [brandId, setBrandId] = useState<string | undefined>();

    const {
      options: brandsOptions,
      isLoading: isBrandsLoading,
      onSearch: onBrandsSearch,
    } = useBrandsSelectOptions();

    const handleSelectBrandId = (value?: string) => {
      setBrandId(value);
      form.setFieldsValue({ model: undefined });
    };

    const {
      options: modelsOptions,
      isLoading: isModelsLoading,
      onSearch: onModelsSearch,
    } = useModelsSelectOptions(brandId);

    const handleResetBrandId = () => {
      setBrandId(undefined);
      form.setFieldsValue({ model: undefined });
      onBrandsSearch('');
      onModelsSearch('');
    };

    useEffect(() => {
      const emptyFieldValues = getEmptyFieldValues(form);

      form.resetFields();
      form.setFieldsValue({
        ...emptyFieldValues,
        ...initialValues,
        ...(isAdmin
          ? {
            contractorId: 'contractorId' in initialValues ? initialValues.contractorId : contractorId,
            autoparkId: 'autoparkId' in initialValues ? initialValues.autoparkId : autoparkId,
          }
          : isManager
            ? {
              contractorId,
              autoparkId: 'autoparkId' in initialValues ? initialValues.autoparkId : autoparkId,
            }
            : { contractorId, autoparkId }
        ),
      });
    }, [contractorId, autoparkId, form, initialValues, isAdmin, isManager]);

    const defaultValues = useMemo(
      () => ({
        contractorId: initialValues.contractorId ?? contractorId,
        autoparkId: initialValues.autoparkId ?? autoparkId,
        brand: initialValues.brand,
        model: initialValues.model,
        year: initialValues.year,
        status: initialValues.status ? initialValues.status[0] : undefined,
      }),
      [contractorId, autoparkId, initialValues]
    );

    const handleFinish = (values: FormValues) => {
      const formattedValues: Partial<TransportSearchRequest> = {
        ...values,
        year: values.year ? +values.year : undefined,
        status: values.status ? [values.status] : undefined,
      };

      onFinish(formattedValues);
    };

    const handleBlurYear = (event: FocusEvent<HTMLInputElement>) => {
      const { value } = event.target;

      if (value) {
        if (+value < 1950) form.setFieldsValue({ year: 1950 });
        else if (+value > new Date().getFullYear()) form.setFieldsValue({ year: new Date().getFullYear() });
      }
    };

    const onValuesChange = (changedValues: FormValues) => {
      if ('contractorId' in changedValues) {
        form.setFieldsValue({ autoparkId: undefined });
      }
    };

    return (
      <Form
        className={styles.filtersForm}
        form={form}
        initialValues={defaultValues}
        onValuesChange={onValuesChange}
        onFinish={handleFinish}
      >
        <Form.Item
          name="contractorId"
          label="Автопарк"
          className={styles.formItem}
        >
          <AutoparkSelect
            showSearch
            allowClear
            disabled={!isAdmin}
          />
        </Form.Item>
        <Form.Item dependencies={['contractorId']} noStyle>
          {({ getFieldValue }) => {
            const contractorId = getFieldValue('contractorId');

            return (
              <Form.Item
                name="autoparkId"
                label="Филиал"
                className={styles.formItem}
              >
                <BranchSelect
                  autoparkId={contractorId}
                  disabled={!contractorId || !(isAdmin || isManager)}
                  placeholder={`Выберите ${contractorId ? 'филиал' : 'автопарк'}`}
                  showSearch
                  allowClear
                />
              </Form.Item>
            );
          }}
        </Form.Item>
        <Form.Item
          name="brand"
          label={t.Transport.filter.brand.title}
          className={styles.formItem}
        >
          <Select
            allowClear
            showSearch
            filterOption={false}
            size="small"
            placeholder={t.Transport.filter.brand.placeholder}
            options={!isBrandsLoading ? brandsOptions : []}
            notFoundContent={isBrandsLoading ? <Spin size="small" /> : null}
            onSearch={onBrandsSearch}
            onSelect={value => handleSelectBrandId(value?.toString())}
            onClear={handleResetBrandId}
          />
        </Form.Item>
        <Form.Item
          name="model"
          label={t.Transport.filter.model.title}
          className={styles.formItem}
        >
          <Select
            allowClear
            showSearch
            size="small"
            disabled={!brandId}
            filterOption={false}
            placeholder={t.Transport.filter.model.placeholder}
            options={!isModelsLoading ? modelsOptions : []}
            notFoundContent={isModelsLoading ? <Spin size="small" /> : null}
            onSearch={onModelsSearch}
            onClear={() => onModelsSearch('')}
          />
        </Form.Item>
        <Form.Item
          name="year"
          label={t.Transport.filter.year.title}
          normalize={(value, prevValue) => (/^\d*$/.test(value) && value.length < 5 ? value : prevValue ?? '')}
          className={styles.formItem}
        >
          <Input
            className={styles.input}
            placeholder={t.Transport.filter.year.placeholder}
            onBlur={handleBlurYear}
          />
        </Form.Item>
        <Form.Item
          name="status"
          label={t.Transport.filter.status.title}
          className={styles.formItem}
        >
          <Select
            className={styles.select}
            allowClear
            size="small"
            placeholder={t.Transport.filter.status.placeholder}
            options={statusOptions}
          />
        </Form.Item>
      </Form>
    );
  };
