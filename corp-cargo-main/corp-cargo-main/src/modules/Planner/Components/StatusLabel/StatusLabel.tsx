import React, { FC } from 'react';

import { CreatorTypeType, RouteStatusType } from '../../types';
import * as S from './StatusLabel.style';

interface Properties {
  name: string;
  right: string;
  color: string;
  backgroundColor?: string;
  border?: string;
}

interface Props {
  status: RouteStatusType | CreatorTypeType;
  indicators: Record<string, Properties>;
}

export const StatusLabel: FC<Props> = props => {
  const { status, indicators } = props;

  return (
    <S.Container
      right={indicators[status]?.right}
      color={indicators[status]?.color}
      backgroundColor={indicators[status]?.backgroundColor}
      border={indicators[status]?.border}
    >
      {indicators[status]?.name}
    </S.Container>
  );
};
