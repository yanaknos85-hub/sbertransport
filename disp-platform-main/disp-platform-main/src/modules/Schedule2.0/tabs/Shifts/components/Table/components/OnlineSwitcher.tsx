import React, { FC } from 'react';

import { Switch } from '@sber-sbertransport/ui-kit/src';

import { DriverStatus } from 'api/schedule2.0/schedule.types';
import { useUpdateCargoDriverOnline } from 'api/trips-cargo/trips-cargo.api';
import { useUpdateDriverOnline } from 'api/trips/trips.api';

import { TripTypes } from 'constants/app.constants';

import { ignore } from 'utils/utils';

interface OnlineSwitcherProps {
  data?: DriverStatus;
  vehicleType: TripTypes;
}

export const OnlineSwitcher: FC<OnlineSwitcherProps> = ({ data, vehicleType }) => {
  const [updateOnline, { isLoading: isLoadingPass }] = useUpdateDriverOnline();
  const [updateCargoOnline, { isLoading: isLoadingCargo }] = useUpdateCargoDriverOnline();

  const onChangeOnline = () => {
    if (!data?.driverId) return;

    const updateFunc = vehicleType === TripTypes.Cargo
      ? updateCargoOnline
      : updateOnline;

    updateFunc({ driverId: data?.driverId })
      .catch(ignore);
  };

  return (
    <Switch
      checked={!!data?.online}
      onChange={onChangeOnline}
      loading={isLoadingPass || isLoadingCargo}
      disabled={!data?.driverId}
    />
  );
};
