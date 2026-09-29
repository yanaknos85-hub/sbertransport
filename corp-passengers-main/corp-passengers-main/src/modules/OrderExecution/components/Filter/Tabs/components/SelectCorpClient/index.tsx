import { SelectProps } from 'antd/lib/select';
import React from 'react';
import { TripStatus } from 'api/travel-status';
import { Status } from 'stores/Engineer/Models/Feed/Feed.status';
import { APIQueryResult } from 'api';
import { useOrganizations } from 'api/organizations';
import Select from '../../../Inputs/Select';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { statusHook: APIQueryResult<TripStatus[], Error> };

// TODO: pagination
const SelectCorpClient: React.FC<Props<Status['name']>> = ({ statusHook, ...selectProps }) => {
  const { content: organizations } = useOrganizations().data.organizationResponse;
  const options = organizations.map(org => ({ label: org.officialName, value: org.id }));

  return <Select {...selectProps} options={options} />;
};

export default SelectCorpClient;
