import React from 'react';
import { DatePicker } from 'antd';
import { FormInstance } from 'antd/lib/form';
import classNames from 'classnames';

import { DateRangeFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import FormText from './FormText';
import { DATE_FORMAT } from 'constants/app.constants';
import { FormItem } from 'components/FormItem/FormItem';
import styles from './DateRangeField.module.scss';

const getDateFormat = ({ format, showTime }: DateRangeFieldProps) => {
  if (format) {
    return format;
  }
  return showTime ? DATE_FORMAT.DATE_WITH_TIME : DATE_FORMAT.BASE_REVERTED_DOTS;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const convertToString = (value: any, props: DateRangeFieldProps) => {
  const format = getDateFormat(props);
  return Array.isArray(value) ? value.map(v => v.format(format)).join(' - ') : '';
};

export default (props: DateRangeFieldProps & { form: FormInstance }): JSX.Element => {
  const {
    fieldType, name, editable, label, description, className, rules, showTime = true, ...other
  } = props;
  if (!editable) {
    return <FormText {...props} convertToString={value => convertToString(value, props)} />;
  }

  const validationRules = useValidationRules(props);
  return (
    <FormItem
      name={name}
      label={label}
      shouldUpdate
      rules={validationRules}
    >
      <DatePicker.RangePicker
        style={{ width: '100%' }}
        format={getDateFormat(props)}
        showTime={showTime}
        dropdownClassName="no-now-btn"
        className={classNames(styles.dateRangeField, className)}
        {...other}
      />
    </FormItem>
  );
};
