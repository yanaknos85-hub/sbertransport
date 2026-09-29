import React, { FC } from 'react';
import {
  Checkbox, Form, Input, InputNumber
} from 'antd';
import { FormInstance } from 'antd/es/form';
import cn from 'classnames';

import { Translation, useTranslation } from 'i18n';
import styles from './styles.module.scss';

import { ValidationRules } from '../../fieldValidationRules';
import { useNumericRange } from './useNumericRange';

const defaultFormatter = (value: string | number | undefined = '') => value.toString();
interface Props {
  name: keyof Translation['Forms']['registryFilterFields'];
  label?: keyof Translation['Forms']['registryFilterFields'];
  form: FormInstance;
  formatter?: (value: string | number | undefined) => string;
  bothFieldsRequired?: boolean;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void;
  defaultChecked?: boolean;
  className?: string;
  disabled?: boolean;
}

export const NumericRange: FC<Props> = ({
  form,
  name,
  label,
  className,
  formatter = defaultFormatter,
  bothFieldsRequired = true,
  onClearButtonActivator,
  defaultChecked = false,
  disabled = false,
}) => {
  const {
    checked,
    onCheckChange,
    isStartRequired,
    isEndRequired,
    startValue,
    onStartChange,
    checkRequirement,
    onEndChange,
  } = useNumericRange(bothFieldsRequired, onClearButtonActivator, defaultChecked);

  const { t } = useTranslation();
  const renderInputGroup = () => (
    <Input.Group compact>
      <Form.Item
        name={[name, 'start']}
        rules={[
          ValidationRules.general.validationFloatingNumbers(),
          ValidationRules.general.minInt(name),
          ValidationRules.general.maxInt(name),
          isStartRequired
            ? ValidationRules.general.required
            : () => ({
              validator(_) {
                return checkRequirement(isStartRequired);
              },
            }),
        ]}
      >
        <InputNumber
          min={startValue}
          disabled={disabled}
          placeholder={t.Forms.registryFilterFields.intervalFrom}
          onChange={onStartChange}
          formatter={formatter}
        />
      </Form.Item>
      <span className={styles.dash}>-</span>
      <Form.Item
        name={[name, 'end']}
        rules={[
          ValidationRules.general.validationFloatingNumbers(),
          ValidationRules.general.minInt(name),
          ValidationRules.general.maxInt(name),
          isEndRequired
            ? ValidationRules.general.required
            : () => ({
              validator(_) {
                return checkRequirement(isEndRequired);
              },
            }),
        ]}
      >
        <InputNumber
          min={startValue}
          disabled={disabled}
          placeholder={t.Forms.registryFilterFields.intervalTo}
          formatter={formatter}
          onChange={onEndChange}
        />
      </Form.Item>
    </Input.Group>
  );

  const renderSingleNumeric = () => (
    <Form.Item
      name={name}
      rules={[
        ValidationRules.general.validationFloatingNumbers(),
        ValidationRules.general.minInt(name),
        ValidationRules.general.maxInt(name),
      ]}
      className={styles.inputLarge}
    >
      <InputNumber
        disabled={disabled}
        formatter={formatter}
        placeholder={t.Forms.registryFilterFields.interval}
        onChange={onClearButtonActivator}
      />
    </Form.Item>
  );
  return (
    <Form.Item label={t.Forms.registryFilterFields[label ?? name]} className={cn(styles.numberRange, className)}>
      <Form.Item name={name} className={styles.checkbox}>
        <Checkbox
          checked={checked}
          disabled={disabled}
          onChange={e => {
            form.resetFields([name]);
            if (onClearButtonActivator) {
              onClearButtonActivator(e);
            }
            onCheckChange(!checked);
          }}
          defaultChecked={defaultChecked}
        >
          {t.Forms.registryFilterFields.interval}
        </Checkbox>
      </Form.Item>
      {checked ? renderInputGroup() : renderSingleNumeric()}
    </Form.Item>
  );
};
