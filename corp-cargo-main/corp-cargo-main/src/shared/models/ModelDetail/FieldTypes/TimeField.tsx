import React from 'react';
import { Form, TimePicker } from 'antd';
import classNames from 'classnames';
import { DATE_FORMAT } from 'constants/constants.app';
import useValidationRules from './useValidationRules';

import styles from '../modelDetail.module.scss';

const TimeField = (props: {
  name: string;
  label: string;
  editable: boolean;
  className?: string;
  isNewDesign: boolean;
}) => {
  const {
    name, label, isNewDesign,
  } = props;
  const validationRules = useValidationRules(props);

  return (
    <Form.Item
      name={name}
      label={label}
      colon={false}
      rules={validationRules}
      className={classNames({ [styles.formItem]: isNewDesign })}
    >
      <TimePicker
        style={{
          width: '100%', maxWidth: '280px', height: '48px',
        }}
        placeholder="Выберите время"
        format={DATE_FORMAT.TIME_BASE_SHORT}
      />
    </Form.Item>
  );
};

export default TimeField;
