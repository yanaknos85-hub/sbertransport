import React, { ChangeEvent, KeyboardEvent } from 'react';
import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import InputMask, { Props } from 'react-input-mask';

import { ignore, preventDefault } from 'utils/utils';
import cn from 'classnames';

interface OwnProps {
  placeholder: string;
  mask: string;
  disabled?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  allowClear?: boolean;
  onPressEnter?: (e: KeyboardEvent<HTMLInputElement>) => void;
  className?: string;
}
const CustomInput = ({
  value,
  onChange = ignore,
  onPressEnter = preventDefault,
  className,
  ...rest
}: OwnProps): JSX.Element => {
  const {
    mask, placeholder, allowClear,
  } = { ...rest };

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => onChange(e.target.value);

  return (
    <InputMask
      mask={mask}
      value={value}
      disabled={false}
      onChange={handleChange}
    >
      {(inputProps: Props & InputProps) => (
        <Input
          {...inputProps}
          className={cn(className, inputProps.className)}
          placeholder={placeholder}
          value={value}
          allowClear={allowClear}
          onPressEnter={onPressEnter}
        />
      )}
    </InputMask>
  );
};
export default CustomInput;
