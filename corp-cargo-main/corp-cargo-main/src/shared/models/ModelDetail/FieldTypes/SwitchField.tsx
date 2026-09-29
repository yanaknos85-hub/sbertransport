import React from 'react';
import { Form, Switch } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { SwitchFieldProps } from '../ModelFormField';
import styles from '../modelDetail.module.scss';

export default (props: SwitchFieldProps) => {
  const {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    fieldType, name, editable, options, label, rules, onChange, initialValue, ...other
  } = props;
  if (!editable) {
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

  return (
    <Form.Item
      name={name}
      shouldUpdate
      initialValue={initialValue}
    >
      <div className={styles.switchWithLabel}>
        <Switch defaultChecked={initialValue} onChange={onChange} />
        {label}
      </div>
    </Form.Item>
  );
};
