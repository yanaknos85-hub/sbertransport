import React, { ComponentProps, FC } from 'react';

import { DriverSpecialityTypes, driverSpecialityTitles } from 'constants/driver.constants';
import { Select } from '../Select';

const options = Object.values(DriverSpecialityTypes).map(type => ({
  label: driverSpecialityTitles[type as DriverSpecialityTypes],
  value: type,
}));

export const SelectDriverSpeciality: FC<ComponentProps<typeof Select>> = props => (
  <Select {...props} options={options} />
);
