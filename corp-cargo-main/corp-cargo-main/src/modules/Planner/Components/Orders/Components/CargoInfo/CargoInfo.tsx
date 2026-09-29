import React, { FC } from 'react';
import { DefaultValues, emptySign } from 'constants/constants.app';
import { ReactComponent as Weight } from '../../../../images/weightIcon.svg';
import { ReactComponent as Value } from '../../../../images/volumeIcon.svg';
import { ReactComponent as Capacity } from '../../../../images/capacityIcon.svg';
import { declOfNumForPlaces } from 'utils/declOfNum';

import * as S from './CargoInfo.styles';

interface Props {
  weightR: number | DefaultValues;
  volumeR: number | DefaultValues;
  occupiedPlacesCount?: number | null;
}

export const CargoInfo: FC<Props> = props => {
  const { weightR, volumeR, occupiedPlacesCount = null } = props;

  return (
    <S.CargoInfo>
      <S.CargoParams>
        <Weight />
        <p>
          {weightR} кг
        </p>
      </S.CargoParams>
      <S.CargoParams>
        <Value />
        <p>
          <span>
            {volumeR} м
          <sup>3</sup>
          </span>
        </p>
      </S.CargoParams>
      <S.CargoParams>
        <Capacity />
        <p>
            {occupiedPlacesCount == null
              ? emptySign
              : `${occupiedPlacesCount} ${declOfNumForPlaces(occupiedPlacesCount)}`}
        </p>
      </S.CargoParams>
    </S.CargoInfo>
  );
};
