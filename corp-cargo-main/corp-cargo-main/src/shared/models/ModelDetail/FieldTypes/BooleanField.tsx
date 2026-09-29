/* eslint-disable no-use-before-define */
import React, { FC, useCallback, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { Form, Select } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { SelectProps } from 'antd/lib/select';

import { BooleanFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';

export default (props: BooleanFieldProps) => {
  const {
    fieldType, name, editable, label, ...other
  } = props;
  if (!editable) {
    return <ReadonlyBooleanField {...props} />;
  }

  const validationRules = useValidationRules(props);
  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
      rules={validationRules}
      valuePropName="value"
    >
      <BooleanSelect {...other} />
    </Form.Item>
  );
};

const ReadonlyBooleanField: FC<BooleanFieldProps> = ({
  label, name, ...other
}) => {
  const { t } = useTranslation();
  const {
    trueLabel, falseLabel, unsetLabel,
  } = { ...t.BooleanField, ...other };

  return (
    <Form.Item label={label} shouldUpdate>
      {(form: FormInstance) => {
        const value = form.getFieldValue(name);
        if (value === undefined || value === null) {
          return unsetLabel;
        }
        return value ? trueLabel : falseLabel;
      }}
    </Form.Item>
  );
};

type BooleanSelectProps = SelectProps<{ value: string; label: string }> &
  Pick<BooleanFieldProps, 'trueLabel' | 'falseLabel' | 'unsetLabel'> & {
    value?: boolean | undefined;
    onChange?: (value: boolean | undefined) => void;
    className?: string;
  };

const BooleanSelect: React.FC<BooleanSelectProps> = props => {
  const {
    value, defaultValue, onChange, onSelect, onDeselect, falseLabel, trueLabel, unsetLabel, ...other
  } = props;
  const options = useBooleanSelectOptions(props);
  const selectProps = { ...other, value: value?.toString() ?? undefined };

  const handleChange = useCallback((v: string) => onChange && onChange(toBoolean(v)), [onChange]);

  // TODO: propagate onSelect/onDeselect and defaultValue
  return (
    <Select
      {...selectProps}
      options={options}
      onChange={handleChange}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

const useBooleanSelectOptions = ({
  trueLabel,
  falseLabel,
}: Pick<BooleanFieldProps, 'required' | 'trueLabel' | 'falseLabel' | 'unsetLabel'>) => {
  const { t } = useTranslation();
  return useMemo(
    () => [
      { value: 'true', label: trueLabel || t.BooleanField.trueLabel },
      { value: 'false', label: falseLabel || t.BooleanField.falseLabel },
    ],
    [t, trueLabel, falseLabel]
  );
};

const toBoolean = (value: string): boolean | undefined => {
  switch (value) {
    case 'true':
      return true;
    case 'false':
      return false;
    default:
      return undefined;
  }
};
