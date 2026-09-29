import * as React from 'react';
import { Select } from 'antd';

import { Eco, ecoClasses } from 'api/vehicles/vehicles.constants';
import { getEcoClassDescription } from 'modules/Vehicles/utils';

type Props = React.ComponentProps<typeof Select>;

export const SelectEcoClass = ({ ...props }: Props): JSX.Element => {
  const options = ecoClasses.map(({ name: value, rusName: label }) => ({
    value,
    label: getEcoClassDescription(label as Eco),
  }));

  return (
    <Select
      {...props}
      options={options}
      allowClear
      optionFilterProp="label"
    />
  );
};
