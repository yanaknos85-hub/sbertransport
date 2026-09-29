import { Select } from 'antd';
import * as React from 'react';
import { SelectProps } from 'antd/lib/select';
import { TaxiClassShortDescriptions, taxiTypeShort } from '../../../stores/Trip/Trip.interface';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { restrict?: T[] };

const SelectTaxiType: React.FC<Props<taxiTypeShort>> = ({ restrict, ...selectProps }) => {
  const options = Object.entries(TaxiClassShortDescriptions).map(arr => ({ value: arr[0], label: arr[1] }));

  return (
    <Select
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

export default SelectTaxiType;
