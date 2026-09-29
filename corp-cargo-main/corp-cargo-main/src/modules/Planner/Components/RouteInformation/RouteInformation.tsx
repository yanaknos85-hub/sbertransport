import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { useTranslation } from 'i18n';
import { convertToRubles, formatRubles } from 'utils';
import { RouteParameters } from '../RouteDetailed/RouteParams/RouteParameters';
import { ReactComponent as Question } from '../../images/question.svg'

import * as S from './RouteInformation.styles';

interface Props {
  distance: number;
  volume: number;
  weight: number;
  cost: number;
}

export const RouteInformation: FC<Props> = props => {
  const {
    distance, volume, weight, cost,
  } = props;

  const { t } = useTranslation();

  return (
    <S.Wrapper>
      <RouteParameters
        distance={distance}
        volume={volume}
        weight={weight}
      />
      <S.Divider />
      <S.Price>
        <S.Title>
          <p>{t.Planner.totalCost}</p>
        </S.Title>
        <p>{formatRubles(convertToRubles(cost))}</p>
      </S.Price>
      <S.Divider />
    </S.Wrapper>
  );
};
