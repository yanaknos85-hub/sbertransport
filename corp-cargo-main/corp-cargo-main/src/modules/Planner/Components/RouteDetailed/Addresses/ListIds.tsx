import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { RouteWaypointType } from '../../../types';
import * as S from './Addresses.style';

interface Props {
  point: RouteWaypointType;
}

export const ListIds: FC<Props> = props => {
  const { point } = props;

  const tooltipTitle = point?.humanReadbleIds?.map(id => id).join(', ');

  if (!point.humanReadbleIds) {
    return null;
  }

  if (point.humanReadbleIds.length > 1) {
    return (
      <Tooltip placement="topLeft" title={tooltipTitle}>
        <S.Title>{`${point.humanReadbleIds[0]}...`}</S.Title>
      </Tooltip>
    );
  }

  return <S.Title>{point?.humanReadbleIds[0]}</S.Title>;
};
