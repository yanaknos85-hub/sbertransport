import React from 'react';
import { Form, Select } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { SelectFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';

export default (props: SelectFieldProps & { initialValue?: string | number }): JSX.Element => {
  const {
    fieldType, name, editable, label, description, disabled, rules, initialValue, onChange, ...other
  } = props;
  // eslint-disable-next-line react/destructuring-assignment
  const options = typeof props.options === 'function' ? props.options() : props.options;

  if (!editable) {
    // TODO like a FormText?
    const renderText = (form: FormInstance) => {
      const getLabel = (value: string) => options.find(opt => opt.value === value)?.label ?? value;

      const fieldValue = form.getFieldValue(name);
      if (typeof fieldValue === 'string') {
        return getLabel(fieldValue);
      }

      return (fieldValue || []).map(getLabel).join(', ');
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
      initialValue={initialValue}
    >
      <Select
        showSearch
        allowClear
        optionFilterProp="label"
        onChange={onChange}
        {...other}
        options={options}
        disabled={disabled}
        getPopupContainer={trigger => trigger.parentNode}
      />
    </Form.Item>
  );
};
