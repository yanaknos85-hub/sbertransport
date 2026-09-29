import React, { FC } from 'react';

import { useTranslation } from 'i18n';
import uuid from 'utils/uuid';
import * as S from './OrderDetailedListItem.styles';
import { OrderWaypointType } from '../../../types';
import { renderPointType } from '../../utils';

interface Props {
  list: OrderWaypointType[];
}

export const OrderDetailedListItem: FC<Props> = props => {
  const { list } = props;

  const { t } = useTranslation();

  return (
    <>
      <S.Title>{t.Planner.routeTitle}</S.Title>
      <S.List>
        {list.map((point, idx) => (
          <S.Card key={uuid()}>
            <S.Title>{point?.id}</S.Title>
            <S.Content>
              <S.Status>
                {idx + 1}
                .
                {renderPointType(point.type)}
              </S.Status>
              <S.Address>{point.addressStringRepresentation}</S.Address>
              <S.Link href="#">{t.Planner.onMap}</S.Link>
            </S.Content>
          </S.Card>
        ))}
      </S.List>
    </>
  );
};
