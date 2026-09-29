import React, { useState, FC } from 'react';
import { Form, DatePicker } from 'antd';
import moment from 'moment';
import { DATE_FORMAT } from 'constants/constants.app';
import { useTranslation } from 'i18n';
import { LabelInterval } from './LabelInterval';
import styles from './switchComponents.module.scss';

const SingleDatePicker: FC<{
  value?: [moment.Moment, moment.Moment];
  onChange?: React.Dispatch<[moment.Moment, moment.Moment] | null>;
  isDisabledFutureDate?: boolean;
}> = ({
  value, onChange, isDisabledFutureDate = false,
}) => (
  <DatePicker
    value={value && value[0]}
    format={DATE_FORMAT.BASE}
    disabledDate={d => isDisabledFutureDate && d.isAfter(moment().endOf('day'))}
    onChange={value => onChange && onChange(value && [value, value])}
  />
);

export const DateRangeSwitch: FC<{
  isDisabledFutureDate?: boolean;
  name: string;
  label: string;
}> = ({
  name, label, isDisabledFutureDate = false,
}) => {
  const { t } = useTranslation();
  const [checked, onCheckChange] = useState<boolean>(false);

  return checked ? (
    <Form.Item
      className={styles.dataRange}
      name={name}
      label={(
        <LabelInterval
          checked={checked}
          checkBoxLabel={t.Forms.registryFilterFields.periodRange}
          label={label}
          onCheckChange={() => onCheckChange(!checked)}
        />
      )}
    >
      <DatePicker.RangePicker disabledDate={d => isDisabledFutureDate && d.isAfter(moment().endOf('day'))} />
    </Form.Item>
  ) : (
    <Form.Item
      className={styles.dataRange}
      name={name}
      label={(
        <LabelInterval
          checked={checked}
          checkBoxLabel={t.Forms.registryFilterFields.periodRange}
          label={label}
          onCheckChange={() => onCheckChange(!checked)}
        />
      )}
    >
      <SingleDatePicker isDisabledFutureDate={isDisabledFutureDate} />
    </Form.Item>
  );
};
