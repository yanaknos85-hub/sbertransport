import React, { FC, ChangeEvent, useState } from 'react';
import { Form, Input, InputProps } from 'antd';
import { FormInstance } from 'antd/es/form';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';

interface VINFieldProps {
  form: FormInstance;
  className?: string;
  inputProps?: InputProps;
}

const VINField: FC<VINFieldProps> = ({
  form, className, inputProps,
}) => {
  const [vinCode, setVinCode] = useState<string>(form.getFieldValue('vinCode'));

  const handleVINChanged = (event: ChangeEvent<HTMLInputElement>) => {
    const { value } = event.target;

    if (!/^[A-Za-z0-9]+$/i.test(value) || value.length > 17) {
      form?.setFieldsValue({ vinCode: vinCode });
    } else {
      setVinCode(value);
    }
  };

  return (
    <Form.Item
      name="vinCode"
      label="VIN номер"
      rules={[
        ValidationRules.general.required,
        ValidationRules.general.checkVin,
      ]}
      normalize={value => value?.toUpperCase()}
      className={className}
    >
      <Input
        allowClear
        placeholder="W09ХХХХХХХХYYYХХХ"
        onChange={handleVINChanged}
        {...inputProps}
      />
    </Form.Item>
  );
};

export default VINField;
