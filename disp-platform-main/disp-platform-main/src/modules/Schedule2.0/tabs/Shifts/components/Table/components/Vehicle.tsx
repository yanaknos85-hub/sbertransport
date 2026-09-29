import React, { FC } from 'react';

import { Tooltip } from 'antd';

import { ScheduleVehicle } from 'api/schedule2.0/schedule.types';

import { ReactComponent as Warning } from 'assets/icons/warning.svg';

import styles from '../Table.module.scss';

const getVehicleName = (vehicle: ScheduleVehicle) => [vehicle.model.brand, vehicle.model.name].filter(Boolean).join(' ');

interface VehicleProps {
  vehicle: ScheduleVehicle;
  isVehicleBooked: boolean;
}

export const Vehicle: FC<VehicleProps> = ({ vehicle, isVehicleBooked }) => {
  const vehicleName = getVehicleName(vehicle);

  return (
    <>
      {isVehicleBooked && (
        <Tooltip title={() => (
          <>
            <div>Новая бронь авто</div>
            <div>Назначьте водителя</div>
          </>
        )}
        >
          <Warning className={styles.infoIcon} />
        </Tooltip>
      )}
      <div className={styles.vehicleName} title={vehicleName}>{vehicleName}</div>
      <div>{vehicle.stateNumber}</div>
    </>
  );
};
