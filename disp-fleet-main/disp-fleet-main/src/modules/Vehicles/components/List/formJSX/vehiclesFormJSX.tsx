import React, { useEffect, useMemo } from 'react';
import { Form, Input, InputNumber } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Store } from 'antd/lib/form/interface';
import { Translation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { useSelfAutopark } from 'api/contractors/contractors.api';
import { SelectVehicleType } from 'components/SelectVehicleType/SelectVehicleType';
import { SelectStateNumberMask } from 'components/SelectStateNumberMask/SelectStateNumberMask';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { getEmptyFieldValues } from 'utils/form';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { useRoleMap } from 'hooks/useRoleMap';
import styles from './formJSX.module.scss';

const {
  checkStateNumber, validationFloatingNumbers, maxInt, minManufactureYear, minLength, maxLength,
}
  = ValidationRules.general;

export const vehiclesFormJSX
  = (t: Translation) => (
    form: FormInstance,
    initialValues: Record<string, unknown>,
    onFinish: (values: Record<string, unknown>) => void
  ): JSX.Element => {
    const { isAdmin, isManager } = useRoleMap();
    const { contractorId, autoparkId } = useProfile().data;

    const { isInternal } = useSelfAutopark().data;

    useEffect(() => {
      const emptyFieldValues = getEmptyFieldValues(form);

      form.resetFields();
      form.setFieldsValue({
        ...emptyFieldValues,
        ...initialValues,
        autoparkId: initialValues.autoparkId ?? autoparkId,
      });
    }, [form, initialValues, autoparkId]);

    const handleFinish = (values: Store) => {
      const formattedValues = { ...values, stateNumber: values.stateNumber?.replace(/_/g, '') };

      onFinish(formattedValues);
    };

    const defaultValues = useMemo(
      () => ({
        ...initialValues,
        autoparkId: initialValues.autoparkId ?? autoparkId,
      }),
      [autoparkId, initialValues]
    );

    return (
      <Form
        className={styles.filtersForm}
        form={form}
        initialValues={defaultValues}
        onFinish={handleFinish}
      >
        <Form.Item
          className={styles.formItem}
          name="autoparkId"
          label={t.Vehicles.autoparkName}
        >
          <BranchSelect
            autoparkId={contractorId}
            className={styles.select}
            showDivider={false}
            placeholder={t.Vehicles.selectAutoPark}
            disabled={!(isAdmin || isManager) && isInternal}
            allowClear
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="stateNumber"
          label={t.Vehicles.stateNumber}
          rules={[checkStateNumber]}
        >
          <SelectStateNumberMask className={styles.input} />
        </Form.Item>

        <Form.Item
          name="manufactureYear"
          className={styles.formItem}
          label={t.Vehicles.manufactureYear}
          rules={[validationFloatingNumbers(), minLength(4), minManufactureYear(1900), maxInt('manufactureYear')]}
        >
          <InputNumber
            className={styles.input}
            min={1900}
            name="manufactureYear"
            maxLength={4}
            step={1}
            placeholder={t.Vehicles.enterManufactureYear}
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="brand"
          label={t.Vehicles.brand}
          rules={[maxLength(128)]}
        >
          <Input
            allowClear
            className={styles.input}
            placeholder={t.Vehicles.enterBrand}
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="model"
          label={t.Vehicles.model}
          rules={[maxLength(128)]}
        >
          <Input
            allowClear
            className={styles.input}
            placeholder={t.Vehicles.enterModel}
          />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="vehicleType"
          label={t.Vehicles.type}
          rules={[maxLength(128)]}
        >
          <SelectVehicleType className={styles.select} placeholder={t.Vehicles.selectAutoPark} />
        </Form.Item>
      </Form>
    );
  };
