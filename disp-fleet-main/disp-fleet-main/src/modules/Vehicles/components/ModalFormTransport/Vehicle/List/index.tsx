import React, { FC, useMemo } from 'react';
import { Empty } from 'antd';
import { useTranslation } from 'i18n';

import { VehicleListContentItem } from 'api/directories/directories.types';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import VehicleCard, { IVehicleCardData } from './Card';

import styles from './styles.module.scss';

interface IProps {
  vehicle: string | undefined;
  vehicleList: VehicleListContentItem[];
  selectVehicle: (vehicle: string) => void;
  isLoading: boolean;
}

const VehicleList: FC<IProps> = ({
  vehicle, vehicleList, selectVehicle, isLoading,
}) => {
  const {
    t: {
      Transport: {
        directory: { types },
      },
    },
  } = useTranslation();

  const transformedData: IVehicleCardData[] = useMemo(
    () => vehicleList
      ? vehicleList.map(elem => ({
        id: elem.id,
        brand: elem.brand,
        model: elem.model,
        engineType: elem.engineType,
        engineCapacity: elem.engineCapacity.toString(),
        enginePower: elem.enginePower.toString(),
        fuelType: elem.fuelType,
        fuelTankVolume: elem.fuelTankVolume.toString(),
        mudguards: elem.mudguardInstalled ? 'Да' : 'Нет',
        holder: elem.spareWheelHolderInstalled ? 'Да' : 'Нет',
        drive: elem.drive,
        manufacturePeriod: elem.manufacturePeriod,
        transmissionType: elem.transmissionType,
        bodyType: elem.bodyType,
        weight: elem.weight,
        dimensions: elem.dimensions,
      }))
      : [],
    [vehicleList]
  );

  return (
    <div className={styles.content}>
      <div className={styles.header}>
        <div className={styles.header__content}>
          <span className={styles.vehicle}>{types.vehicles.subject}</span>

          <span className={styles.manufacturePeriod}>{types.vehicles.fields.manufacturePeriod}</span>

          <span className={styles.bodyType}>{types.vehicles.fields.bodyType}</span>

          <span className={styles.engine}>{types.vehicles.fields.engineCombinedTitle}</span>

          <span className={styles.fuel}>{types.vehicles.fields.fuelCombinedTitle}</span>

          <span className={styles.mudguards}>{types.vehicles.fields.mudguards}</span>

          <span className={styles.holder}>{types.vehicles.fields.spareWheelHolder}</span>

          <span className={styles.drive}>{types.vehicles.fields.wheelDrive}</span>

          <span className={styles.transmissionType}>{types.vehicles.fields.transmissionType}</span>

          <span className={styles.weight}>{types.vehicles.fields.weight}</span>

          <span className={styles.dimensions}>{types.vehicles.fields.dimensions}</span>
        </div>
      </div>

      {isLoading ? (
        <SpinWrapped />
      ) : !transformedData.length ? (
        <Empty />
      ) : (
        transformedData.map(elem => (
          <VehicleCard
            key={elem.id}
            selected={vehicle === elem.id}
            data={elem}
            onClick={() => selectVehicle(elem.id)}
          />
        ))
      )}
    </div>
  );
};

export default VehicleList;
