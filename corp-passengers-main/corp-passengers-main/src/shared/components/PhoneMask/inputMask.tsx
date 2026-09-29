import React, { ChangeEvent, KeyboardEvent } from 'react';
import { Input } from 'antd';

import InputMask, { Props } from 'react-input-mask';
import { InputProps } from 'antd/lib/input';
import { ignore, preventDefault } from 'utils';
import NewDesignInput from 'shared/form/Input/Input';

interface OwnProps {
  placeholder: string;
  mask: string;
  disabled?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  allowClear?: boolean;
  onPressEnter?: (e: KeyboardEvent<HTMLInputElement>) => void;
  isNewDesign?: boolean;
}
const CustomInput = ({
  value,
  onChange = ignore,
  onPressEnter = preventDefault,
  isNewDesign,
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
      {(inputProps: Props & InputProps) => isNewDesign ? (
        <NewDesignInput
          {...inputProps}
          placeholder={placeholder}
          value={value}
          allowClear={allowClear}
          onPressEnter={onPressEnter}
        />
      ) : (
        <Input
          {...inputProps}
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
