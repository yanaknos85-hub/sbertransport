import React from 'react';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';

import RestrictedInput from 'components/RestrictedInput';
import { TextFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';

export default (
  props: TextFieldProps & {
    form: string & FormInstance;
    type?: 'email' | 'password';
  }
): JSX.Element => {
  const {
    fieldType, name, editable, type, label, form, onChange, ...other
  } = props;

  if (!editable) {
    const renderText = (form: FormInstance) => {
      let value = form.getFieldValue(name);
      if (type === 'password') {
        value = value.replace(/./g, '*');
      }
      return <span>{value}</span>;
    };

    return (
      <Form.Item label={label} shouldUpdate>
        {renderText}
      </Form.Item>
    );
  }

  const validationRules = useValidationRules(props);

  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
      rules={validationRules}
    >
      <RestrictedInput
        autoComplete="off"
        {...other}
        name={name}
        form={form}
        onClearButtonActivator={onChange}
      />
    </Form.Item>
  );
};
