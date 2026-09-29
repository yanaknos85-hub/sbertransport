import React from 'react';
import { Form, InputNumber } from 'antd';
import { NumberFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import FormText from './FormText';

export default (props: NumberFieldProps & { initialValue?: string | number }): JSX.Element => {
  const {
    fieldType, name, editable, label, description, rules, required, initialValue, ...other
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
    >
      <InputNumber style={style} {...other} />
    </Form.Item>
  );
};
