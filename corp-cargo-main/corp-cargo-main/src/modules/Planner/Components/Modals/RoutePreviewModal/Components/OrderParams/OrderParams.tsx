import React, { FC } from 'react';
import { convertToRubles, formatRubles } from 'utils';
import { DefaultValues } from 'constants/constants.app';

import { useTranslation } from 'i18n';
import { ReactComponent as Weight } from '../../../../../images/weightIcon.svg';
import { colors, fontFamily } from 'shared/styles/styles';
import { ReactComponent as Value } from '../../../../../images/volumeIcon.svg';

import * as S from './OrderParams.styles';

interface Props {
  weightR: number | DefaultValues;
  volumeR: number | DefaultValues;
  cost: number;
}

export const OrderParams: FC<Props> = props => {
  const {
    weightR, volumeR, cost,
  } = props;

  const { t } = useTranslation();

  return (
    <S.OrderParams>
      <S.CargoInfo>
        <S.CargoParams>
          <Weight />
          <S.SemiBoldText fontFamily={fontFamily.SBSansInterface} color={colors.gray10}>
            {weightR}
            кг
          </S.SemiBoldText>
          <S.Text>{t.Planner.mass}</S.Text>
        </S.CargoParams>
        <S.CargoParams>
          <Value />
          <S.SemiBoldText fontFamily={fontFamily.SBSansInterface} color={colors.gray10}>
            <span>
              {volumeR}
              м
              <sup>3</sup>
            </span>
          </S.SemiBoldText>
          <S.Text>{t.Planner.volume}</S.Text>
        </S.CargoParams>
      </S.CargoInfo>
      <S.Price>
        <p>{t.Planner.deliveryCost}</p>
        <p>{formatRubles(convertToRubles(cost))}</p>
      </S.Price>
    </S.OrderParams>
  );
};
