import React from 'react';
import { Form, InputNumber } from 'antd';
import cn from 'classnames';
import NewInputNumber from 'shared/form/InputNumber/InputNumber';
import { NumberFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import FormText from './FormText';
import styles from '../modelDetail.module.scss';

export default (props: NumberFieldProps & { initialValue?: string | number; isNewDesign?: boolean }): JSX.Element => {
  const {
    fieldType, name, editable, label, description, rules, required, initialValue, isNewDesign, ...other
  } = props;
  if (!editable) {
    return <FormText {...props} />;
  }

  const style = {
    width: '100%',
  };

  const validationRules = useValidationRules(props);
  return (
    <Form.Item
      name={name}
      label={label}
      initialValue={initialValue}
      rules={validationRules}
      className={cn({ [styles.formItem]: isNewDesign })}
      colon={!isNewDesign}
    >
      {isNewDesign ? <NewInputNumber {...other} /> : <InputNumber style={style} {...other} />}
    </Form.Item>
  );
};
