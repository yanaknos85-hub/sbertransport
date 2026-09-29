import React from 'react';
import { FormInstance } from 'antd/lib/form';
import { Form } from 'antd';
import { ModelFormFieldProps } from '../ModelFormField';

type FormTextProps = ModelFormFieldProps & {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  convertToString?: (value: any) => string;
};

export default (props: FormTextProps): JSX.Element => {
  const {
    name, label, convertToString,
  } = props;
  const renderText = (form: FormInstance) => {
    const value = form.getFieldValue(name);
    return <span>{convertToString ? convertToString(value) : value}</span>;
  };

  return (
    <Form.Item label={label} shouldUpdate>
      {renderText}
    </Form.Item>
  );
};
