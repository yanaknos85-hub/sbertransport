import React, { FC } from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Spin } from 'antd';
import { OrderListMinimalType, OrderWaypointMinimalType, WaypointType } from '../../../../types';
import { OrderListItem } from '../OrderListItem/OrderListItem';
import { NotFoundData } from '../../../NotFoundData';
import * as S from './OrdersList.style';

interface Props {
  orderId?: string;
  isRouteVisible?: boolean;
  isFetchingOrders?: boolean;
  handleChangeListItem?: (e: any) => void;
  handleOrderWaypoints?: (waypoints: OrderWaypointMinimalType[], id: string) => void;
}

export const OrdersList: FC<Props> = props => {
  const { orderId, isRouteVisible, isFetchingOrders, handleChangeListItem, handleOrderWaypoints } = props;
  const { plannerStore } = useAppStoreContext();
  const { ordersListStore } = plannerStore;
  return (
    <S.ListWrapper>
      {isFetchingOrders ? (
        <Spin />
      ) : ordersListStore?.content?.length ? (
        ordersListStore.content.map((order: OrderListMinimalType) => (
          <OrderListItem
            key={order.id}
            handleChange={handleChangeListItem}
            draggable={isRouteVisible}
            handleOrderWaypoints={handleOrderWaypoints}
            active={order.id === orderId}
            {...order}
          />
        ))
      ) : (
        <NotFoundData />
      )}
    </S.ListWrapper>
  );
};
