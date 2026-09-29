import { PersonalCar } from '@sber-sbertransport/mf-core';
import { Radio } from 'antd';
import classNames from 'classnames';
import React, { FC } from 'react';

import { MOTORCYCLE } from 'constants/constants.env';

import styles from '../../../TaxiClasses/Card/card.module.scss';
import { useIsEditUrl } from '../../hooks/useIsEditUrl';
import personalCarsStyles from './personalCars.module.scss';
import { Item } from './styled';

interface IPersonalCarCard {
  car: PersonalCar;
  cost: JSX.Element | null;
}

export const PersonalCarCard: FC<IPersonalCarCard> = ({ car, cost }): JSX.Element => {
  const { isEditUrl } = useIsEditUrl();
  const isMotorcycle = car.transportType === MOTORCYCLE;

  const radioClass = classNames(styles.card, styles.card__label, 'no-input-radio');

  return (
    <Radio
      className={radioClass}
      value={car}
      disabled={isEditUrl}
      style={{ display: 'flex', flexDirection: 'column' }}
    >
      <div className="personal-card-content">
        <div className={classNames(isMotorcycle ? 'motorcycleIcon' : 'carIcon')}> </div>
        <div className={personalCarsStyles.carInfo}>
          <Item>
            <span className={personalCarsStyles.icon}> </span>
          </Item>
          <Item>
            <span className={personalCarsStyles.carBrand}>{car.brandName}</span>
            <span className={personalCarsStyles.textOverflow}>{car.model}</span>
            <span className={classNames(personalCarsStyles.regNumber, personalCarsStyles.textOverflow)}>
              {car.registrationNumber}
            </span>
          </Item>
          <Item>
            <span>{cost}</span>
          </Item>
        </div>
      </div>
    </Radio>
  );
};
