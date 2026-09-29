import React, { FC } from 'react';
import { Divider } from 'antd';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { getRoundedParams } from 'utils/getRoundedParams';
import { OrderListMinimalType, OrderWaypointMinimalType } from '../../../../types';
import { Header } from '../Header/Header';
import { OrderInfo } from '../OrderInfo/OrderInfo';
import { Footer } from '../Footer/Footer';
import { AddressBlockDetailed } from 'modules/Planner/Components/AddressBlockDetailed/AddressBlockDetailed';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { getOrderCoordinates } from '../../../utils';
import * as S from './OrderListItem.styles';

type Props = {
  handleChange?: (e: CheckboxChangeEvent) => void;
  draggable?: boolean;
  handleOrderWaypoints?: (waypoints: OrderWaypointMinimalType[], id: string) => void;
  active?: boolean;
} & OrderListMinimalType;

export const OrderListItem: FC<Props> = observer((props) => {
  const {
    id, humanReadableId, desiredDate, waypoints, volume, weight, occupiedPlacesCount,
    handleChange, draggable, handleOrderWaypoints, active,
    contractorName, comment, segments,
  } = props;

  const { setCoordinates } = usePlanner();
  const { plannerStore } = useAppStoreContext();

  const { weight: weightR, volume: volumeR } = getRoundedParams({ weight, volume });

  const handleDragStart = (e: React.DragEvent<HTMLDivElement>, orderList: OrderListMinimalType[], orderId: string) => {
    if (!plannerStore.checkedOrdersListStore.length) {
      plannerStore.dragOrder(orderId, orderList);
    }
  };

  const isChecked = (orderId: string): boolean => {
    return plannerStore.checkedOrdersListStore.some((order: OrderListMinimalType) => order.id === orderId);
  };

  const onChange = (e: CheckboxChangeEvent, orderId: string) => {
    handleChange?.(e);
  };

  const handleClick = () => {
    handleOrderWaypoints?.(waypoints, id);
    setCoordinates(
      segments && segments.length > 0
        ? segments.reduce<Array<{ latitude: number; longitude: number }>>((acc, el) => {
          if (el.coordinates) acc.push(...el.coordinates);
          return acc;
        }, [])
        : getOrderCoordinates(waypoints)
    );
  };

  return (
    <S.Container
      draggable={draggable}
      onDragStart={(e: React.DragEvent<HTMLDivElement>) => handleDragStart(e, plannerStore.ordersListStore?.content ?? [] as OrderListMinimalType[], id)}
      onClick={handleClick}
    >
      <S.ListItem active={isChecked(id) || active}>
        <S.ListItemBlock>
          <S.HeaderBlock>
            <Header
              desiredDate={desiredDate}
              humanReadableId={humanReadableId}
              onClick={(e) => e.stopPropagation()}
              value={id}
              contractorName={contractorName}
            />
          </S.HeaderBlock>
          <S.OrderInfoBlock>
            <OrderInfo
              weightR={weightR} volumeR={volumeR} occupiedPlacesCount={occupiedPlacesCount}
              onClick={(e) => e.stopPropagation()}
              checked={isChecked(id)} value={id}
              onChange={(e: CheckboxChangeEvent) => onChange(e, id)}
            />
          </S.OrderInfoBlock>
        </S.ListItemBlock>
        <Divider style={{ margin: '8px' }} />
        <div>
          <AddressBlockDetailed waypoints={waypoints} />
        </div>
        {comment ? (
          <>
            <Divider style={{ margin: '8px 0' }} />
            <Footer comment={comment} />
          </>
        ) : null}
      </S.ListItem>
    </S.Container>
  );
});
