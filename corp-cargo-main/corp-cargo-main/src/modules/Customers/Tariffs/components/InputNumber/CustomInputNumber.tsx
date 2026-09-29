import React, { forwardRef } from 'react';
import { InputNumber, Button, InputNumberProps } from 'antd';
import { MinusOutlined, PlusOutlined } from '@ant-design/icons';

import styles from './customInputNumber.module.scss';

interface CustomInputNumberProps extends Omit<InputNumberProps, 'onChange' | 'value'> {
  value?: number;
  onChange?: (value: number | null) => void;
  min?: number;
  max?: number;
  step?: number;
  disabled?: boolean;
  precision?: number;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const CustomInputNumber = forwardRef<any, CustomInputNumberProps>(
  ({
    value = 0, onChange, min = 0, max = 1, step = 0.1, disabled = false, precision = 1, ...rest
  }, ref) => {
    const roundToPrecision = (num: number): number => {
      const factor = Math.pow(10, precision);
      return Math.round(num * factor) / factor;
    };

    const handleMinus = () => {
      if (!disabled && typeof value === 'number') {
        const newValue = roundToPrecision(value - step);
        if (newValue >= min) {
          onChange?.(newValue);
        } else {
          onChange?.(min);
        }
      }
    };

    const handlePlus = () => {
      if (!disabled && typeof value === 'number') {
        const newValue = roundToPrecision(value + step);
        if (newValue <= max) {
          onChange?.(newValue);
        } else {
          onChange?.(max);
        }
      }
    };

    const handleChange = (val: number | string | null) => {
      if (val === null) {
        onChange?.(null);
        return;
      }

      let num: number;
      if (typeof val === 'string') {
        num = parseFloat(val);
        if (isNaN(num)) {
          return;
        }
      } else {
        num = val;
      }

      const roundedValue = roundToPrecision(num);
      if (roundedValue < min) {
        onChange?.(min);
      } else if (roundedValue > max) {
        onChange?.(max);
      } else {
        onChange?.(roundedValue);
      }
    };

    return (
      <div className={styles.wrapper}>
        <InputNumber
          ref={ref}
          value={value}
          onChange={handleChange}
          controls={false}
          bordered={false}
          min={min}
          max={max}
          step={step}
          disabled={disabled}
          className={styles.input}
          {...rest}
        />
        <div className={styles.controls}>
          <Button
            onClick={handleMinus}
            icon={<MinusOutlined />}
            size="small"
            type="text"
            disabled={disabled || (typeof value === 'number' && value <= min)}
          />
          <Button
            onClick={handlePlus}
            icon={<PlusOutlined />}
            size="small"
            type="text"
            disabled={disabled || (typeof value === 'number' && value >= max)}
          />
        </div>
      </div>
    );
  }
);

export default CustomInputNumber;
