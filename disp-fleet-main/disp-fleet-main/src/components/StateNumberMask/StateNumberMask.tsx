import React, { ChangeEvent, KeyboardEvent } from 'react';
import InputMask, { Props } from 'react-input-mask';
import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import cn from 'classnames';

import { StateNumberMasks } from 'constants/app.constants';
import { ignore, preventDefault } from 'utils/utils';
import { ALLOWED_VEHICLE_REGISTRY_CHARS } from 'utils/fieldValidationRules/fieldValidationRules';

interface OwnProps {
  placeholder?: string;
  disabled?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  allowClear?: boolean;
  onPressEnter?: (e: KeyboardEvent<HTMLInputElement>) => void;
  className?: string;
  mask?: string;
}

export const StateNumberMask = ({
  value,
  onChange = ignore,
  onPressEnter = preventDefault,
  placeholder = 'А123АА 123 RUS',
  mask = StateNumberMasks.Passenger,
  className,
  allowClear,
}: OwnProps): JSX.Element => {
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value.toUpperCase());
  };

  return (
    <InputMask
      mask={mask}
      value={value?.toUpperCase()}
      disabled={false}
      onChange={handleChange}
      // @ts-ignore TS2604
      formatChars={{ A: ALLOWED_VEHICLE_REGISTRY_CHARS, 9: '[0-9]' }}
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
