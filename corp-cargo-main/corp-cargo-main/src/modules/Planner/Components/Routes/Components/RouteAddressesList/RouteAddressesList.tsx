import React, { FC } from 'react';
import { AddressBlockDetailed_New } from '../../../AddressBlockDetailed_New/AddressBlockDetailed_New';
import { RouteWaypointType } from '../../../../types';

import { ReactComponent as Truck } from '../../../../images/truck.svg';

import * as S from './RouteAddressesList.styles';

interface Props {
  waypoints: RouteWaypointType[];
  transportType: string;
}

export const RouteAddressesList: FC<Props> = props => {
  const { waypoints, transportType } = props;

  return (
    <S.ListItemAddressStyled>
      <AddressBlockDetailed_New waypoints={waypoints} />
      <div>
        {transportType === 'DEDICATED' ? <Truck /> : null}
      </div>
    </S.ListItemAddressStyled>
  );
};
