import React from 'react';
import { Form, Checkbox } from 'antd';
import { FormInstance } from 'antd/lib/form';

import { CheckboxGroupFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';

export default (props: CheckboxGroupFieldProps) => {
  const {
    fieldType, name, editable, options, label, rules, ...other
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
      rules={validationRules}
    >
      <Checkbox.Group {...other} options={options} />
    </Form.Item>
  );
};
