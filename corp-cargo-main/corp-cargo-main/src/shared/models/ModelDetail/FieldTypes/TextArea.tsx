import React from 'react';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';

import TextArea from 'antd/lib/input/TextArea';
import { TextAreaFieldProps } from '../ModelFormField';

export default (props: TextAreaFieldProps): JSX.Element => {
  const {
    name, editable, label, rules, ...other
  } = props;

  if (!editable) {
    // TODO like a FormText?
    const renderText = (form: FormInstance) => {
      const value = form.getFieldValue(name);
      // if (type === 'password') {
      //   value = value.replace(/./g, '*');
      // }
      return <span>{value}</span>;
    };

    return (
      <Form.Item label={label} shouldUpdate>
        {renderText}
      </Form.Item>
    );
  }

  // const validationRules = rules && useValidationRules(rules);

  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
    >
      <TextArea autoComplete="off" {...other} />
    </Form.Item>
  );
};
