import React, { FC, useEffect, useMemo } from 'react';
import { FormInstance } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import moment from 'moment';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { DATE_FORMAT } from 'constants/constants.app';

import styles from './DateFieldComponent.module.scss';

interface Props {
  label?: string;
  form?: FormInstance;
}

export const DateFieldComponent: FC<Props> = ({ label = 'Дата и время отправления', form }) => {
  const { [StoreNames.cargoStore]: cargoStore } = useAppStoreContext();

  const getDefaultDate = () => {
    const now = moment();
    if (cargoStore.isRelocation) {
      return now.add(2, 'days').startOf('day');
    } else {
      return now.add(1, 'days').startOf('day');
    }
  };

  const getDisabledDate = (current: moment.Moment): boolean => {
    const now = moment();
    if (cargoStore.isRelocation) {
      return current < now.add(2, 'days').startOf('day') || current > now.add(1, 'years');
    } else {
      return current < now.add(1, 'days').startOf('day') || current > now.add(1, 'years');
    }
  };

  useEffect(() => {
    if (form) {
      const newDate = getDefaultDate();
      form.setFieldsValue({
        desiredDate: newDate,
      });
    }
  }, [cargoStore.isRelocation, form]);

  const fieldConfig = useMemo(() => ({
    desiredDate: {
      name: 'desiredDate',
      type: FieldType.date,
      initialValue: getDefaultDate(),
      rules: [ValidationRules.general.required],
      params: {
        disabledDate: getDisabledDate,
        format: DATE_FORMAT.DATE_WITH_TIME_DOUBLE_HOURS,
        showTime: true,
        showNow: false,
      },
    },
  }), [cargoStore.isRelocation]);

  return (
    <div className={styles.border}>
      <FormField label={label} {...fieldConfig.desiredDate} />
      {cargoStore.isRelocation && (
        <div className={styles.infoText}>
          Не ранее чем через 2 дня после оформления заявки
        </div>
      )}
    </div>
  );
};
