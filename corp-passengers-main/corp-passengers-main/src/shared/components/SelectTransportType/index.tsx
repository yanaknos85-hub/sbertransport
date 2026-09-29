import { Select } from 'antd';
import { useTransportTypes } from 'api/transport-types';
import React from 'react';
import { TransportType } from 'stores/TransportTypes/TransportTypes.interface';
import { SelectProps } from 'antd/lib/select';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { restrict?: T[] };

const SelectTransportType: React.FC<Props<TransportType['name']>> = ({ restrict, ...selectProps }) => {
  const { data: transportTypes } = useTransportTypes();
  const availableTransportTypes = transportTypes.filter(({ name }) => !restrict || restrict.includes(name));
  const options = availableTransportTypes.map(({ name: value, rusName: label }) => ({ label, value }));

  return (
    <Select
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

export default SelectTransportType;
