import React, { FC } from 'react';
import { colors, fontFamily } from 'shared/styles/styles';
import { useTranslation } from 'i18n';
import * as S from './AddressesList.styles';
import { OrderListMinimalType } from '../../../../../types';

interface Props {
  checkedOrders: OrderListMinimalType[];
}

export const AddressesList: FC<Props> = props => {
  const { checkedOrders } = props;

  const { t } = useTranslation();

  return (
    <>
      <S.ListTitle>{t.Planner.addresses}</S.ListTitle>
      {checkedOrders.map(order => (
        <S.AddressesList fontFamily={fontFamily.SBSansInterface} color={colors.black}>
          <ul>
            {order.waypoints.map(point => (
              <li>{`${point.addressStringRepresentation}`}</li>
            ))}
          </ul>
        </S.AddressesList>
      ))}
    </>
  );
};
