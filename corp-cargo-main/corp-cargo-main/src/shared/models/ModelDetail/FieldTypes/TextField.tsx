/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';

import RestrictedInput from 'shared/components/RestrictedInput';
import cn from 'classnames';
import { TextFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import styles from '../modelDetail.module.scss';

export default (
  props: TextFieldProps & {
    form: string & FormInstance<any>;
    type?: 'email' | 'password';
    allowURL?: boolean;
    isNewDesign?: boolean;
  }
): JSX.Element => {
  const {
    fieldType, name, editable, type, label, form, onChange, ...other
  } = props;

  if (!editable) {
    // TODO like a FormText?
    const renderText = (form: FormInstance) => {
      let value = form.getFieldValue(name);
      if (type === 'password') {
        value = value.replace(/./g, '*');
      }
      return <span>{value}</span>;
    };

    return (
      <Form.Item
        label={label}
        shouldUpdate
        className={cn({ [styles.formItem]: other.isNewDesign })}
        colon={!other.isNewDesign}
      >
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
      className={cn({ [styles.formItem]: other.isNewDesign })}
      colon={!other.isNewDesign}
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
