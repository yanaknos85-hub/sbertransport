import { Select } from 'antd';
import * as React from 'react';
import { SelectProps } from 'antd/lib/select';
import { DATE_TYPES, dateType, DateTypeDescriptions } from 'stores/DateTypes/DateTypes.interface';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { restrict: DATE_TYPES[] };

const SelectDateTimeType: React.FC<Props<dateType | string>> = ({ restrict, ...selectProps }) => {
  const options = Object.entries(DateTypeDescriptions)
    .map(arr => ({ value: arr[0], label: arr[1] }))
    .filter(({ value }) => !restrict.includes(value as DATE_TYPES));
  return (
    <Select
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

export default SelectDateTimeType;
