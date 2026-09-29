import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import React, { FC } from 'react';

export const NumericInput: FC<
  Omit<InputProps, 'onChange'> & { onChange?: React.Dispatch<number | undefined> }
> = props => {
  const _onChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { value } = e.target;
    const reg = /[^0-9.]/g;
    const { onChange } = props;

    if (value === '') {
      if (onChange) {
        onChange(undefined);
      }
    } else {
      const result = value.replace(reg, '');
      if (onChange) {
        onChange(+result);
      }
    }
  };

  return <Input {...props} onChange={_onChange} />;
};
