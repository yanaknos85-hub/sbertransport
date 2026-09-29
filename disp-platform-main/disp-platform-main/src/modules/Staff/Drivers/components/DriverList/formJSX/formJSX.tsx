import React, { useEffect } from 'react';
import {
  Form, Input, InputNumber, Select
} from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Translation } from 'i18n';
import { reject } from 'ramda';

import { useGetDriverLicenses } from 'api/drivers/drivers.api';
import { DRIVER_ACTIVE_DESCRIPTIONS } from 'constants/driver.constants';
import { DRIVER_ACTIVE } from 'types/drivers';
import { getDriverLicenseOptions } from 'modules/Staff/Drivers/utils';
import { SelectDriverSpeciality } from 'components/SelectDriverSpeciality';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import styles from './formJSX.module.scss';

const {
  floatTwoSymbols, max, minMaxLength, checkTrimmedField,
} = ValidationRules.general;

export const activeOptions = Object.entries(DRIVER_ACTIVE_DESCRIPTIONS).map(([value, label]) => ({
  value,
  label,
}));

export const formJSX
  = (t: Translation) => (
    form: FormInstance,
    initialValues: Record<string, unknown>,
    onFinish: (values: Record<string, unknown>) => void
  ): JSX.Element => {
    const driverLicenses = useGetDriverLicenses();
    const driverLicenseOptions = getDriverLicenseOptions(driverLicenses);

    useEffect(() => {
      form.setFieldsValue({
        ...initialValues,
        ...('ratingFrom' in initialValues && { ratingFrom: Number(initialValues.ratingFrom) / 100 }),
        ...('isActive' in initialValues && {
          isActive: initialValues.isActive ? DRIVER_ACTIVE.ACTIVE : DRIVER_ACTIVE.NOT_ACTIVE,
        }),
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [initialValues]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const formatValues = (values: any) => {
      const formattedValues = {
        ...values,
        ...(Boolean(values.ratingFrom) && { ratingFrom: Math.round(values.ratingFrom * 100) }),
        ...(Boolean(values.isActive) && { isActive: values.isActive === DRIVER_ACTIVE.ACTIVE }),
      };
      onFinish(reject(x => typeof x === 'undefined' || x === '')(formattedValues));
    };

    return (
      <Form
        className={styles.filtersForm}
        form={form}
        initialValues={initialValues}
        onFinish={formatValues}
      >
        <Form.Item
          className={styles.formItem}
          name="driverFullName"
          label={t.Requests.List.searchPanel}
          rules={[minMaxLength(3, 128), checkTrimmedField()]}
        >
          <Input className={styles.input} />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          label={t.Drivers.Labels.rating}
          name="ratingFrom"
          rules={[floatTwoSymbols, max(5)]}
        >
          <InputNumber
            className={styles.input}
            placeholder={t.Drivers.Labels.enterRating}
            step={0.01}
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          label={t.Drivers.Labels.licenseClasses}
          name="driverLicenses"
        >
          <Select
            className={styles.select}
            options={driverLicenseOptions}
            allowClear
            placeholder={t.Drivers.Labels.selectLicenseClasses}
            mode="multiple"
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          label={t.Drivers.Labels.active}
          name="isActive"
        >
          <Select
            className={styles.select}
            options={activeOptions}
            allowClear
            placeholder={t.Drivers.Labels.selectActive}
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="driverSpeciality"
          label={t.Drivers.Labels.speciality}
        >
          <SelectDriverSpeciality className={styles.select} placeholder={t.Drivers.Labels.speciality} />
        </Form.Item>
      </Form>
    );
  };
