import { CheckboxChangeEvent } from 'antd/lib/checkbox/Checkbox';
import { Checkbox } from 'antd';
import React, { FC } from 'react';

interface Props {
  isDisabled: boolean;
  checked: boolean;
  name?: string;
  onChange: (event: CheckboxChangeEvent) => undefined | void;
}

export const UniversalCargoCheckbox: FC<Props>= (props) => {

  const { isDisabled, checked, name, onChange } = props;

  return (
    <Checkbox
      disabled={isDisabled}
      checked={checked}
      name={name ?? 'universal'}
      onChange={onChange}
    >Сделать груз общим</Checkbox>
  )
};
