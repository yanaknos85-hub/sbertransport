import React, { FC } from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { colors, fontFamily } from 'shared/styles/styles';

import * as S from './IdsList.styles';

export const IdsList: FC = () => {
  const { plannerStore } = useAppStoreContext();

  return (
    <S.RouteParams>
      {plannerStore.checkedOrdersListStore.map(order => (
        <S.SemiBoldText fontFamily={fontFamily.SBSansTextSemibold} color={colors.black}>
          {order.humanReadableId}
        </S.SemiBoldText>
      ))}
    </S.RouteParams>
  );
};
