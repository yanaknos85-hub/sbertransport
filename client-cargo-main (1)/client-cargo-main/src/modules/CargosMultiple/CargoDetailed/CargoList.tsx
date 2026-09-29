import React, { FC } from 'react';
import { observer } from 'mobx-react';
import CargoListComponent from 'shared/components/Cargo/CargoList';
import styled from 'styled-components';

import { CargoListItem } from 'types/Cargo';
import { getVolume } from 'utils/Misc';

import styles from '../static/Cargos.module.scss';

const Container = styled.div`
  display: flex;
  flex-wrap: wrap;
  flex-direction: column;
  justify-content: flex-start;
  margin-bottom: 32px;
`;

interface Props {
  cargoDetails?: CargoListItem[] | undefined;
  senderAddress: string;
  receiverAddress: string;
  occupiedPlacesCount?: number;
  volume?: number;
  weight?: number;
  isRegular?: boolean;
}

export const CargoList: FC<Props> = observer(props => {
  const {
    cargoDetails, occupiedPlacesCount, volume, weight, isRegular,
  } = props;
  const cargoVolume = isRegular ? cargoDetails?.reduce((acc, cargo) => acc + (cargo.volume * cargo.occupiedPlacesCount), 0) : volume;
  const cargoWeight = isRegular ? cargoDetails?.reduce((acc, cargo) => acc + (cargo.weight * cargo.occupiedPlacesCount), 0) : weight;
  const cargoOccupiedPlacesCount = isRegular ? cargoDetails?.reduce((acc, cargo) => acc + cargo.occupiedPlacesCount, 0) : occupiedPlacesCount;

  return (
    <Container>
      <div className={styles.cargosHeaderBlock}>
        <div className={styles.cargosHeaderText}>
          Доставки
          <div className={styles.cargosHeaderCount}>{cargoOccupiedPlacesCount}</div>
        </div>
        <div>
          {cargoVolume ? getVolume(cargoVolume) : ''}
          ,
          {' '}
          {cargoWeight}
          {' '}
          кг
        </div>
      </div>
      <div className={styles.cargosMainDiv}>
        <CargoListComponent list={cargoDetails} />
      </div>
    </Container>
  );
});
