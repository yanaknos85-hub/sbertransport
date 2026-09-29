/* eslint-disable no-use-before-define */
/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { Form, DatePicker } from 'antd';
import { FormInstance } from 'antd/lib/form';
import classNames from 'classnames';

import { DATE_FORMAT } from 'constants/constants.app';
import { RangePicker } from 'shared/form/DatePicker/DatePicker';
import { DateRangeFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import FormText from './FormText';
import styles from '../modelDetail.module.scss';

export default (props: DateRangeFieldProps & { form: FormInstance; isNewDesign?: boolean }): JSX.Element => {
  const {
    fieldType, name, editable, label, description, className, rules, isNewDesign, ...other
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
      className={classNames({ [styles.formItem]: isNewDesign })}
      colon={!isNewDesign}
    >
      {isNewDesign ? (
        <RangePicker
          style={{ width: '100%' }}
          format={getDateFormat(props)}
          showTime
          dropdownClassName="no-now-btn"
          className={classNames('picker', className)}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
          {...other}
        />
      ) : (
        <DatePicker.RangePicker
          style={{ width: '100%' }}
          format={getDateFormat(props)}
          showTime
          dropdownClassName="no-now-btn"
          className={classNames('picker', className)}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
          {...other}
        />
      )}
    </Form.Item>
  );
};

const getDateFormat = ({ format, showTime }: DateRangeFieldProps) => {
  if (format) {
    return format;
  }
  return showTime ? DATE_FORMAT.DATE_WITH_TIME : DATE_FORMAT.BASE_REVERTED;
};

const convertToString = (value: any, props: DateRangeFieldProps) => {
  const format = getDateFormat(props);
  return Array.isArray(value) ? value.map(v => v.format(format)).join(' - ') : '';
};
