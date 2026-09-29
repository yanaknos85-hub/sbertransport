import React, { FC, useMemo, useState } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import { ValidationRules } from 'shared/fieldValidationRules';
import { ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import NumericField from 'shared/models/ModelDetail/FieldTypes/NumericField';
import { declOfNum } from 'utils';
import { DeliveryTimeRow, DeliveryUrgency } from '../types/types';

import styles from '../components/CargoDeliveryTimeSettings/styles.module.scss';

const CellInput: FC<{ name: string; initialValue: number }> = ({ name, initialValue }) => {
  const [value, setValue] = useState(initialValue);
  return (
    <div className={styles.inner_cell}>
      <NumericField
        fieldType={ModelFormFieldType.NUMBER}
        name={name}
        editable
        rules={[ValidationRules.general.onlyDigits]}
        initialValue={initialValue}
        style={{ width: 75 }}
        maxLength={4}
        min={1}
        onChange={value => {
          setValue(value as number);
        }}
      />
      <span className={styles.inner_span}>{declOfNum(value, ['день', 'дня', 'дней'])}</span>
    </div>
  );
};

export const useColumns = (): ColumnProps<DeliveryTimeRow>[] => {
  const { t } = useTranslation();

  return useMemo(
    (): ColumnProps<DeliveryTimeRow>[] => [
      {
        title: t.CargoDeliveryTimeSettings.Columns.urgency,
        dataIndex: 'urgency',
        key: 'urgency',
        width: 200,
        render: () => t.CargoDeliveryTimeSettings.urgencyOptions.standard,
      },
      {
        title: t.CargoDeliveryTimeSettings.Columns.range0_100,
        dataIndex: 0,
        key: 'range0',
        width: 100,
        render: (value, record) => <CellInput name={`${record.urgency}_0`} initialValue={value} />,
      },
      {
        title: t.CargoDeliveryTimeSettings.Columns.range100_500,
        dataIndex: 100,
        key: 'range100',
        width: 100,
        render: (value, record) => <CellInput name={`${record.urgency}_100`} initialValue={value} />,
      },
      {
        title: t.CargoDeliveryTimeSettings.Columns.range500_1000,
        dataIndex: 500,
        key: 'range500',
        width: 100,
        render: (value, record) => <CellInput name={`${record.urgency}_500`} initialValue={value} />,
      },
      {
        title: t.CargoDeliveryTimeSettings.Columns.range1000,
        dataIndex: 1000,
        key: 'range1000',
        width: 100,
        render: (value, record) => value && <CellInput name={`${record.urgency}_1000`} initialValue={value ?? 1}/>
      },
    ],
    [t]
  );
};
