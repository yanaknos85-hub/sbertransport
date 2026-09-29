import React from 'react';
import { Form, DatePicker } from 'antd';
import { FormInstance } from 'antd/lib/form';
import classNames from 'classnames';

import { useTranslation } from 'i18n';
import { DateFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import FormText from './FormText';
import { DATE_FORMAT } from 'constants/app.constants';

const getDateFormat = ({ format, showTime }: DateFieldProps) => {
  if (format) {
    return format;
  }
  return showTime ? DATE_FORMAT.DATE_WITH_TIME : DATE_FORMAT.BASE_REVERTED_DOTS;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const convertToString = (value: any, props: DateFieldProps) => {
  const format = getDateFormat(props);
  return value ? value.format(format) : '';
};

export default (props: DateFieldProps & { form: FormInstance }): JSX.Element => {
  const { t } = useTranslation();

  const {
    fieldType, name, editable, label, description, className, rules, showTime = true, ...other
  } = props;
  if (!editable) {
    return <FormText {...props} convertToString={value => convertToString(value, props)} />;
  }

  const validationRules = useValidationRules(props);
  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
      rules={validationRules}
    >
      <DatePicker
        style={{ width: '100%' }}
        showTime={showTime}
        placeholder={showTime ? t.global.selectDateTime : t.global.selectDate}
        format={getDateFormat(props)}
        dropdownClassName="no-now-btn"
        {...other}
        className={classNames('picker', className)}
      />
    </Form.Item>
  );
};
