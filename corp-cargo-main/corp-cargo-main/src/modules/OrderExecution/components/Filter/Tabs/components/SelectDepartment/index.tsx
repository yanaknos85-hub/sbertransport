import { SelectProps } from 'antd/lib/select';
import React from 'react';
import { TripStatus } from 'api/travel-status';
import { Status } from 'stores/Engineer/Models/Feed/Feed.status';
import { APIQueryResult } from 'api';
import Select from '../../../Inputs/Select';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { statusHook: APIQueryResult<TripStatus[], Error> };

const SelectDepartment: React.FC<Props<Status['name']>> = ({ statusHook, ...selectProps }) => {
  const mockOptions = [{ label: '', value: '' }];

  return <Select {...selectProps} options={mockOptions} />;
};

export default SelectDepartment;
