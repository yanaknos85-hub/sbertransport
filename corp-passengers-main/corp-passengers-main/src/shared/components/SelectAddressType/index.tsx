import { Select } from 'antd';
import * as React from 'react';
import { SelectProps } from 'antd/lib/select';
import { addressType } from 'stores/AddressTypes/AddressTypes.interface';
import { ADDRESS_TYPE, addressTypeDescriptions } from 'stores/Engineer/Models';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { restrict: ADDRESS_TYPE[] };

const SelectAddressType: React.FC<Props<addressType>> = ({ restrict, ...selectProps }) => {
  const options = Object.entries(addressTypeDescriptions)
    .map(arr => ({ value: arr[0], label: arr[1] }))
    .filter(({ value }) => !restrict.includes(value as ADDRESS_TYPE));
  return (
    <Select
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

export default SelectAddressType;
