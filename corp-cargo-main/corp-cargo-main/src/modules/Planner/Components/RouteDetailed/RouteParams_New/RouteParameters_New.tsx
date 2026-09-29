import React, { FC } from 'react';

import { useTranslation } from 'i18n';
import { getRoundedParams } from 'utils/getRoundedParams';

import { ReactComponent as Weight } from '../../../images/weightIcon.svg';
import { ReactComponent as Value } from '../../../images/volumeIcon.svg';
import { ReactComponent as Car } from '../../../images/carIcon.svg';

import { RouteParameter_New } from './RouteParameter_New/RouteParameter_New';
import * as S from './RouteParams_New.styles';

interface Props {
  distance?: number;
  volume?: number;
  weight?: number;
  deliveryTime?: number;
  autoCapacity?: number;
  autoVolume?: number;
}

export const RouteParameters_New: FC<Props> = props => {
  const {
    distance,
    volume,
    weight,
    autoCapacity,
    autoVolume,
  } = props;

  const { t } = useTranslation();

  const {
    weight: weightR,
    volume: volumeR,
    distance: distanceR,
  } = getRoundedParams({
    weight,
    volume,
    distance,
  });

  return (
    <div>
      <S.CargoInfo>
        {!!weight && (
          <RouteParameter_New
            units={{
              quantity: weightR,
              unit: 'кг',
              autoQuantity: autoCapacity
            }}
            icon={<Weight />}
          />
        )}
        {!!volume && (
          <RouteParameter_New
            units={{
              quantity: volumeR,
              unit: 'м',
              autoQuantity:
              autoVolume
            }}
            icon={<Value />}
            isVolume
          />
        )}
        {!!distance && (
          <RouteParameter_New
            units={{quantity: distanceR, unit: 'км'}}
            icon={<Car />}
          />
        )}
      </S.CargoInfo>
    </div>
  );
};
