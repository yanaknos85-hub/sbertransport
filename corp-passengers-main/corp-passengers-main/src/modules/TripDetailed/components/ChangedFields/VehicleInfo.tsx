import { Form, Input } from 'antd';
import React from 'react';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useTranslation } from 'i18n';
import styles from '../../tripDetailView.module.scss';

export const VehicleInfoInput = () => {
  const { t } = useTranslation();
  return (
    <Form.Item
      name="vehicleInfo"
      label={`${t.DetailedView.DispatcherTrip.inputCarInfo}`}
      rules={[ValidationRules.general.maxLength(255)]}
      className={styles.vehicleInfo}
    >
      <Input
        allowClear
        type="text"
        placeholder={`${t.DetailedView.DispatcherTrip.driverAutoInfo}`}
        className={styles.vehicleInfoInput}
      />
    </Form.Item>
  );
};
