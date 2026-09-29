import React from 'react';
import { Form, Radio } from 'antd';
import { FormInstance } from 'antd/lib/form';

import { RadioGroupFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';

export default (props: RadioGroupFieldProps) => {
  const {
    fieldType, name, editable, options, label, rules, onChange, initialValue, ...other
  } = props;
  if (!editable) {
    // TODO like a FormText?
    const renderText = (form: FormInstance) => {
      const values = form.getFieldValue(name) || [];
      const labels = values.map((value: string) => {
        const option = options.find(option => option.value === value);
        return option?.label ?? option;
      });

      return <span>{labels.join(', ')}</span>;
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
      initialValue={initialValue}
      rules={validationRules}
    >
      <Radio.Group
        options={options}
        onChange={onChange}
        {...other}
      />
    </Form.Item>
  );
};
