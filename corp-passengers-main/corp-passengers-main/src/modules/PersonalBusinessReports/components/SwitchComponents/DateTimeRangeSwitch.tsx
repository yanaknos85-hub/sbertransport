import React, { useState, FC } from 'react';
import { Form, DatePicker } from 'antd';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { LabelInterval } from './LabelInterval';

import styles from './switchComponents.module.scss';
import { useTranslation } from 'i18n';

const DateTimePicker: FC<{
  value?: [moment.Moment, moment.Moment];
  onChange?: React.Dispatch<[moment.Moment, moment.Moment] | null>;
  isDisabledFutureDate?: boolean;
}> = ({
  value, onChange, isDisabledFutureDate = false,
}) => (
  <DatePicker
    showTime={{
      hideDisabledOptions: true,
    }}
    value={value && value[0]}
    format={DATE_FORMAT.DATE_WITH_TIME_SECONDS}
    disabledDate={d => isDisabledFutureDate && d.isAfter(moment().endOf('day'))}
    onChange={value => onChange && onChange(value && [value, value])}
  />
);

export const DateTimeRangeSwitch: FC<{
  isDisabledFutureDate?: boolean;
  name: string;
  label: string;
}> = ({
  name, isDisabledFutureDate = false, label,
}) => {
  const [checked, onCheckChange] = useState<boolean>(false);
  const { t } = useTranslation();
  return checked ? (
    <Form.Item
      className={styles.dataRange}
      name={name}
      label={(
        <LabelInterval
          checkBoxLabel={t.Forms.registryFilterFields.periodRange}
          checked={checked}
          label={label}
          onCheckChange={() => onCheckChange(!checked)}
        />
      )}
    >
      <DatePicker.RangePicker
        format={DATE_FORMAT.DATE_WITH_TIME_SECONDS}
        showTime={{ hideDisabledOptions: true }}
        disabledDate={d => isDisabledFutureDate && d.isAfter(moment().endOf('day'))}
      />
    </Form.Item>
  ) : (
    <Form.Item
      className={styles.dataRange}
      name={name}
      label={(
        <LabelInterval
          checkBoxLabel={t.Forms.registryFilterFields.periodRange}
          checked={checked}
          label={label}
          onCheckChange={() => onCheckChange(!checked)}
        />
      )}
    >
      <DateTimePicker isDisabledFutureDate={isDisabledFutureDate} />
    </Form.Item>
  );
};
