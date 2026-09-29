import React, { FC } from 'react';
import { observer } from 'mobx-react';
import CargoListComponent from 'shared/components/Cargo/CargoList';

import { getVolume } from 'utils/Misc';

import { CargoListItem } from '../../../types';

import styles from './CargoList.module.scss';

interface Props {
  cargoDetails: CargoListItem[];
}

export const CargoList: FC<Props> = observer(props => {
  const { cargoDetails } = props;

  const {
    cargoVolume,
    cargoWeight,
    cargoOccupiedPlacesCount,
  } = cargoDetails.reduce((acc, cargo) => ({
    cargoVolume: acc.cargoVolume + (cargo.volume * cargo.occupiedPlacesCount),
    cargoWeight: acc.cargoVolume + (cargo.weight * cargo.occupiedPlacesCount),
    cargoOccupiedPlacesCount: acc.cargoOccupiedPlacesCount + cargo.occupiedPlacesCount,
  }), {
    cargoVolume: 0,
    cargoWeight: 0,
    cargoOccupiedPlacesCount: 0,
  });

  return (
    <div className={styles.cargoMainInnerContent}>
      <div className={styles.cargoListWrapper}>
        <div className={styles.cargosHeaderBlock}>
          <div className={styles.cargosHeaderText}>
            Информация о грузе
            <div className={styles.cargosHeaderCount}>{cargoOccupiedPlacesCount}</div>
          </div>
          <div>
            {`${cargoVolume ? getVolume(cargoVolume) : ''}, ${cargoWeight} кг`}
          </div>
        </div>
        <CargoListComponent list={cargoDetails} />
      </div>
    </div>
  );
});
