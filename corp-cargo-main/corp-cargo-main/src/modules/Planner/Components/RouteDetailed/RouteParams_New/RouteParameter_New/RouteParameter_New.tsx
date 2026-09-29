import React, { FC } from 'react';
import { colors, fontFamily } from 'shared/styles/styles';

import { DefaultValues } from 'constants/constants.app';
import * as S from './RouteParameter_New.styles';

interface Props {
  units: {
    quantity: number | DefaultValues;
    unit: string,
    autoQuantity?: number;
  };
  icon: JSX.Element;
  isVolume?: boolean;
}

export const RouteParameter_New: FC<Props> = props => {
  const {
    units,
    icon,
    isVolume,
  } = props;
  const { quantity, unit, autoQuantity } = units;
  return (
    <S.Wrapper>
      {icon}
      <S.Block
        fontFamily={fontFamily.SBSansInterface}
        color={colors.gray10}
      >
        {isVolume ? (
          <span>
            {quantity}
            {unit}
            <sup>3</sup>/{autoQuantity}
          </span>
        ) : (
          `${quantity}${unit}/${autoQuantity ?? ''}`
        )}
      </S.Block>
    </S.Wrapper>
  );
};
