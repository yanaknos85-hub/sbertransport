import React from 'react';
import type { FC } from 'react';
import classnames from 'classnames';

import specialIcon from 'assets/images/carSpecial.png';
import engineIcon from 'assets/icons/engine.svg';
import fuelIcon from 'assets/icons/fuel.svg';
import mudguardsIcon from 'assets/icons/mudguards.svg';
import holderIcon from 'assets/icons/holder.svg';
import driveIcon from 'assets/icons/drive.svg';
import transmissionIcon from 'assets/icons/transmission.svg';
import styles from './styles.module.scss';

export interface IVehicleCardData {
  id: string;
  brand: string;
  model: string;
  engineType: string;
  engineCapacity: string;
  enginePower: string;
  fuelType: string;
  fuelTankVolume: string;
  mudguards: string;
  holder: string;
  drive: string;
  transmissionType: string;
  manufacturePeriod: string;
  bodyType: string;
  weight: number;
  dimensions: string;
}

interface IProps {
  selected: boolean;
  data: IVehicleCardData;
  onClick: () => void;
}

const VehicleCard: FC<IProps> = ({
  selected, data, onClick,
}) => {
  const {
    brand,
    model,
    engineType,
    engineCapacity,
    enginePower,
    fuelType,
    fuelTankVolume,
    mudguards,
    holder,
    drive,
    transmissionType,
    manufacturePeriod,
    bodyType,
    weight,
    dimensions,
  } = data;

  return (
    <div
      className={classnames(styles.content, selected && styles.content_selected)}
      onClick={onClick}
    >
      <div className={styles.logo}>
        <img src={specialIcon} alt="" />
      </div>

      <div className={styles.vehicle}>
        <span className={styles.brand}>
          {brand}
        </span>

        <span>{model}</span>
      </div>

      <div className={styles.manufacturePeriod}>
        <span>{manufacturePeriod}</span>
      </div>

      <div className={styles.bodyType}>
        <span>{bodyType}</span>
      </div>

      <div className={styles.engine}>
        <img src={engineIcon} alt="engine" />

        <span>{engineType}</span>

        <span className={styles.engine__volume}>
          {engineCapacity}
        </span>

        <span>{enginePower}</span>
      </div>

      <div className={styles.fuel}>
        <img src={fuelIcon} alt="fuel" />

        <span className={styles.fuel__type}>
          {fuelType}
        </span>

        <span>{fuelTankVolume}</span>
      </div>

      <div className={styles.mudguards}>
        <img src={mudguardsIcon} alt="mudguards" />

        <span>{mudguards}</span>
      </div>

      <div className={styles.holder}>
        <img src={holderIcon} alt="holder" />

        <span>{holder}</span>
      </div>

      <div className={styles.drive}>
        <img src={driveIcon} alt="holder" />

        <span>{drive}</span>
      </div>

      <div className={styles.transmissionType}>
        <img src={transmissionIcon} alt="transmission" />

        <span>{transmissionType}</span>
      </div>

      <div className={styles.weight}>
        <span>{weight}</span>
      </div>

      <div className={styles.dimensions}>
        <span>{dimensions}</span>
      </div>

    </div>
  );
};

export default VehicleCard;
