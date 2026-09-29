import React, { FC } from 'react';
import { convertToRubles, formatRubles } from 'utils';
import { useTranslation } from 'i18n';
import { AddressBlockDetailed } from '../../../AddressBlockDetailed/AddressBlockDetailed';
import { RouteWaypointType } from '../../../../types';

import * as S from './Price.styles';

interface Props {
  waypoints: RouteWaypointType[];
  cost: number;
}

export const Price: FC<Props> = props => {
  const { waypoints, cost } = props;

  const { t } = useTranslation();

  return (
    <S.ListItemAddressStyled>
      <AddressBlockDetailed waypoints={waypoints} />
      <S.Price>
        <p>{t.Planner.deliveryCost}</p>
        <p>{formatRubles(convertToRubles(cost))}</p>
      </S.Price>
    </S.ListItemAddressStyled>
  );
};
