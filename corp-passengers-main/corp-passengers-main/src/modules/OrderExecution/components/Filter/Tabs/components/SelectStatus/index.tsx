import { SelectProps } from 'antd/lib/select';
import React from 'react';
import { TripStatus } from 'api/travel-status';
import { Status } from 'stores/Engineer/Models/Feed/Feed.status';
import { APIQueryResult } from 'api';
import { TransportStatuses } from 'modules/ServiceMetrics/TransportStatuses';
import Select from '../../../Inputs/Select';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { statusHook: APIQueryResult<TripStatus[], Error> };

const SelectStatus: React.FC<Props<Status['name']>> = ({ statusHook, ...selectProps }) => {
  const { data: statuses } = statusHook;
  const options = statuses.reduce((acc: { label: string; value: string }[], { name, rusName }) => {
    const item = acc.find(el => el.label === rusName);
    item
      ? (item.value += `,${name}`)
      : acc.push({
        label: rusName,
        value: name,
      });
    return acc;
  }, []);

  const filteredStatuses = options.filter(el => TransportStatuses.cargo.includes(el.value));

  return (
    <Select
      {...selectProps}
      options={filteredStatuses}
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      getPopupContainer={(trigger: { parentNode: any }) => trigger.parentNode}
    />
  );
};

export default SelectStatus;
