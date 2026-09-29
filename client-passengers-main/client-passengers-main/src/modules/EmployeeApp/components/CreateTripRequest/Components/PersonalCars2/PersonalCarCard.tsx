import { PersonalCar } from '@sber-sbertransport/mf-core';
import { Radio } from 'antd';
import classNames from 'classnames';
import React, { FC } from 'react';

import { MOTORCYCLE } from 'constants/constants.env';

import styles from '../../../TaxiClasses/Card/card.module.scss';
import { useIsEditUrl } from '../../hooks/useIsEditUrl';
import personalCarsStyles from './personalCars.module.scss';

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
      value={car.id}
      disabled={isEditUrl}
      style={{ display: 'flex', flexDirection: 'column' }}
    >
      <div className={personalCarsStyles.personalCardContent}>
        <div className={personalCarsStyles.carInfo}>
          <div className={personalCarsStyles.carMainInfo}>
            <div className={personalCarsStyles.carContainerInfo}>
              <div className={personalCarsStyles.carInfoLogo}>
                <span className={personalCarsStyles.carLogo} />
              </div>
              <div className={personalCarsStyles.carInfoBrandOrType}>
                <div className={personalCarsStyles.carInfoBrand}>
                  <span>{car.brandName}</span>
                  <span>{car.model}</span>
                </div>
                <div className={personalCarsStyles.carInfoType}>
                  <span>{isMotorcycle ? 'Мотоцикл' : 'Автомобиль'}</span>
                </div>
              </div>
            </div>
            <div className={personalCarsStyles.carRegistrationNumber}>
              <span>{car.registrationNumber}</span>
              <span className={personalCarsStyles.icon} />
            </div>
          </div>
          <div className={personalCarsStyles.carInfoCostOrSeats}>
            <div className={personalCarsStyles.carInfoCost}>
              <span>{cost}</span>
            </div>
            <div className={personalCarsStyles.carInfoSeatsCount}>
              <span className={personalCarsStyles.seatsLogo} />
              <span>{car.passengerSeatsCount}</span>
            </div>
          </div>
        </div>
      </div>
    </Radio>
  );
};
