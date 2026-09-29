import React, { FC } from 'react';
import { getRoundedParams } from 'utils/getRoundedParams';
import { RouteWaypointType } from '../../../types';

import * as S from './PointCargoParams.style';

interface Props {
  point: Pick<RouteWaypointType, 'weight' | 'volume'>;
}

export const PointCargoParams: FC<Props> = props => {
  const {
    point: { weight, volume },
  } = props;

  const { weight: weightR, volume: volumeR } = getRoundedParams({
    weight,
    volume,
  });

  return (
    <S.Params>
      <p>
        Вес:
        {weightR ?? '-'}
        кг
      </p>
      <p>
        Объём:
        {' '}
        {volumeR ?? '-'}
        м
        <sup>3</sup>
      </p>
    </S.Params>
  );
};
