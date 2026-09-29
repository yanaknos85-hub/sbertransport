import React, { FC } from 'react';

import { getRoundedParams } from 'utils/getRoundedParams';
import { useTranslation } from 'i18n';
import { ReactComponent as Weight } from '../../../images/weightIcon.svg';
import { ReactComponent as Value } from '../../../images/volumeIcon.svg';
import { ReactComponent as Car } from '../../../images/carIcon.svg';

import * as S from './RouteParams.styles';
import { RouteParameter } from './RouteParameter/RouteParameter';

interface Props {
  distance?: number;
  volume?: number;
  weight?: number;
  deliveryTime?: number;
}

export const RouteParameters: FC<Props> = props => {
  const {
    distance, volume, weight,
  } = props;

  const { t } = useTranslation();

  const {
    weight: weightR, volume: volumeR, distance: distanceR,
  } = getRoundedParams({
    weight,
    volume,
    distance,
  });

  return (
    <div>
      <S.Title>{t.Planner.detailing}</S.Title>
      <S.CargoInfo>
        <RouteParameter
          units={{ quantity: weightR, unit: 'кг' }}
          icon={<Weight />}
          text="масса"
        />
        <RouteParameter
          units={{ quantity: volumeR, unit: 'м' }}
          icon={<Value />}
          text="объём"
          isVolume
        />
        <RouteParameter
          units={{ quantity: distanceR, unit: 'км' }}
          icon={<Car />}
          text="расстояние"
        />
      </S.CargoInfo>
    </div>
  );
};
