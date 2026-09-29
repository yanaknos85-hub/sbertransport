import { EmployeeModel } from '@sber-sbertransport/mf-core';

import { Select } from 'antd';
import React, { FC } from 'react';

import { noop } from 'utils';

interface EmployeeAutoCompleteProps {
  list: EmployeeModel[];
  onSelect?(val?: EmployeeModel): void;
  value?: EmployeeModel;
  placeholder?: string;
  onFocus?(event: React.FocusEvent<HTMLElement>): void;
}

const EmployeeAutoComplete: FC<EmployeeAutoCompleteProps> = ({
  list,
  value,
  onSelect = noop,
  placeholder = 'Выберите пользователя',
  onFocus,
}) => {
  const options = list.map(x => ({
    value: x.id,
    label: x.shortName,
  }));

  const initOption = options.find(option => option.value === value?.id);
  const initValue = (initOption && initOption.label) || '';

  const handleSelect = (val: string): void => {
    const item = list.find(x => x.id === val);
    onSelect(item);
  };

  const handleFocus = (event: React.FocusEvent<HTMLInputElement>): void => {
    if (onFocus) {
      onFocus(event);
    }
    event.target.select();
  };

  return (
    <Select
      showSearch={true}
      style={{ width: '100%' }}
      placeholder={placeholder}
      onChange={handleSelect}
      onFocus={handleFocus}
      options={options}
      defaultValue={initValue}
      filterOption={(input, option): boolean => String(option?.label).toLowerCase().includes(input.toLowerCase())}
    />
  );
};

export default EmployeeAutoComplete;
