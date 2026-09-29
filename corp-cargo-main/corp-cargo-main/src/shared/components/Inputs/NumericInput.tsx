import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import React, { FC } from 'react';

const numericRegExp = /[^0-9.]/g;

export const NumericInput: FC<
  Omit<InputProps, 'onChange'> & {
    onChange?: React.Dispatch<number | undefined>;
  }
> = props => {
  const { onChange, onFocus } = props;
  const _onChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    if (!onChange) {
      return;
    }
    const { value } = e.target;

    if (value === '') {
      onChange(undefined);
    } else {
      const result = value.replace(numericRegExp, '');
      onChange(+result);
    }
  };
  // Highlight text on focus
  const handleFocus = (event: React.FocusEvent<HTMLInputElement>) => {
    if (onFocus) {
      onFocus(event);
    }
    event.target.select();
  };
  return (
    <Input
      {...props}
      onChange={_onChange}
      onFocus={handleFocus}
      type="number"
    />
  );
};
