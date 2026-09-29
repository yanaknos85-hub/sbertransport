import React, { ComponentProps, FC } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { TripTypes, vehicleTypeTitles } from 'constants/app.constants';

const options = Object.values(TripTypes).map(type => ({
  label: vehicleTypeTitles[type as TripTypes],
  value: type,
}));

export const SelectVehicleType: FC<ComponentProps<typeof Select>> = props => (
  <Select {...props} options={options} />
);
