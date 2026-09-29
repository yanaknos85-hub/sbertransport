import React, { FC } from 'react';
import { colors, fontFamily } from 'shared/styles/styles';

import { DefaultValues } from 'constants/constants.app';
import * as S from './RouteParameter.styles';

interface Props {
  units: { quantity: number | DefaultValues; unit: string };
  icon: JSX.Element;
  text: string;
  isVolume?: boolean;
}

export const RouteParameter: FC<Props> = props => {
  const {
    units, icon, text, isVolume,
  } = props;
  const { quantity, unit } = units;
  return (
    <S.CargoParams>
      {icon}
      <S.SemiBoldText fontFamily={fontFamily.SBSansInterface} color={colors.gray10}>
        {isVolume ? (
          <span>
            {quantity}
            {unit}
            <sup>3</sup>
          </span>
        ) : (
          `${quantity}${unit}`
        )}
      </S.SemiBoldText>
      <S.Text>{text}</S.Text>
    </S.CargoParams>
  );
};
