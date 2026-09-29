import React, { FC, useMemo, useState } from 'react';
import { CloseCircleFilled, EditOutlined } from '@ant-design/icons';
import { Space } from 'antd';
import { useChangeVehicle } from 'api/trips/trips.api';
import { useProfile } from 'api/profile/profile.api';
import { PassTrip } from 'api/trips/trips.types';
import { TripTransportFilters } from 'api/dispatchers/dispatchers.types';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';
import { PaginationParams } from 'utils/io-ts/pagination';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import VehiclesSelect from '../VehiclesSelect/VehiclesSelect';
import { isVehicleBooked } from './utils';
import styles from './index.module.scss';

const VehicleCell: FC<{ trip: PassTrip }> = ({ trip }) => {
  const [isEdit, setIsEdit] = useState(false);

  const query: Omit<TripTransportFilters, keyof PaginationParams> = useMemo(() => ({
    startDate: trip.expectedStartTime ?? undefined,
    endDate: trip.expectedEndTime ?? undefined,
    timeZone: 'Z',
  }), [trip]);

  const { contractorId } = useProfile().data;
  const [changeVehicle, { isLoading }] = useChangeVehicle(contractorId);

  const onChangeVehicle = (vehicleId: UUID) => {
    changeVehicle({
      tripId: trip.id,
      vehicleId,
    })
      .then(() => setIsEdit(false))
      .catch(ignore);
  };

  const vehicle = trip.vehicle ?? trip.planned?.vehicle;

  if (!vehicle) return <>{EMPTY_CELL_CONTENT}</>;

  if (isEdit) return (
    <Space size={10}>
      <VehiclesSelect
        query={query}
        onChange={val => onChangeVehicle(String(val) as UUID)}
        loading={isLoading}
        className={styles.vehicleSelect}
      />
      <CloseCircleFilled className={styles.icon} onClick={() => setIsEdit(false)} />
    </Space>
  );

  const isBooked = isVehicleBooked(trip);

  return (
    <Space size={10}>
      <div>
        <div>{[vehicle.brand, vehicle.model].filter(Boolean).join(' ')}</div>
        <div>{vehicle.stateNumber}</div>
      </div>
      {/* // Редактирование через карандаш запрещено, если в бронировании авто еще не назачен водитель.
          // Нужно воспользоваться столбцом 'driver' */}
      {isBooked && (!!trip.driver || !!trip.planned?.driver) && (
        <EditOutlined
          className={styles.icon}
          onClick={() => setIsEdit(true)}
        />
      )}
    </Space>
  );
};

export default VehicleCell;
