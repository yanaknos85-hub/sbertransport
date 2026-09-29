import React, { FC, useState, useEffect, DragEvent } from 'react';
import { useTranslation } from 'i18n';
import { Tabs } from 'antd';
import {
  useDeleteAddressPoint,
} from 'api/planner';

import uuid from 'utils/uuid';
import { sortOrders } from '../../utils';

import { TabPane } from 'shared/components/Tabs';
import SpinWrapped from "shared/components/SpinWrapped/SpinWrapped";
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import { RouteType, RouteWaypointType } from '../../../types';
import { Address } from '../Address/Address';

import * as S from './Addresses.style';

interface Props {
  data: RouteType;
  routeId: string;
  updateRoute: (route: RouteType | { tariffId?: string }) => void;
}

export const Addresses: FC<Props> = ({ data, routeId, updateRoute }) => {

  const { waypoints } = data;

  const [deleteAddressPoint] = useDeleteAddressPoint();

  const [currentOrder, setCurrentOrder] = useState<RouteWaypointType>({} as RouteWaypointType);
  const [listOrders, setListOrders] = useState<RouteWaypointType[]>(waypoints);
  const [isLoading, setIsLoading] = useState(false);

  const { logger } = useAppStoreContext();
  const { t } = useTranslation();
  const { Planner: { warnings } } = t;

  useEffect(() => {
    setListOrders(waypoints);
  }, [waypoints]);

  const handleDeleteListItem = async (orderId: string, index: number) => {
    if (listOrders.length <= 2) {
      logger.toMessage('warning', `${warnings.routeLength.description}`);
    } else {
      const filteredList = listOrders.filter(order => order.id !== orderId);

      for (let i = index; i < filteredList.length; i += 1) {
        filteredList[i] = { ...filteredList[i], orderingIndex: filteredList[i].orderingIndex - 1 };
      }

      setIsLoading(true);
      await deleteAddressPoint(orderId)
        .then(() => {
          setListOrders(filteredList);
        })
        .catch(() => {
          setListOrders(prevState => prevState);
        })
        .finally(() => {
          setIsLoading(false);
        })
    }
  };

  const handleListItemUp = async (order: RouteWaypointType, index: number) => {
    if (!index) {
      return;
    }
    setIsLoading(true);
    setCurrentOrder(order);

    const newItemIndex = index - 1;
    const result = [...listOrders];

    result[index] = { ...result[index], orderingIndex: result[index].orderingIndex - 1 };
    result[newItemIndex] = { ...result[newItemIndex], orderingIndex: result[newItemIndex].orderingIndex + 1 };

    try {
      await updateRoute({ ...data, waypoints: result.sort(sortOrders) });
      setListOrders(result);
    } finally {
      setIsLoading(false);
    }
  };

  const handleListItemDown = async (order: RouteWaypointType, index: number) => {
    if (index === waypoints.length - 1) {
      return;
    }

    setIsLoading(true);
    setCurrentOrder(order);

    const newItemIndex = index + 1;
    const result = [...listOrders];

    result[index] = { ...result[index], orderingIndex: result[index].orderingIndex + 1 };
    result[newItemIndex] = { ...result[newItemIndex], orderingIndex: result[newItemIndex].orderingIndex - 1 };

    try {
      await updateRoute({ ...data, waypoints: result.sort(sortOrders) });
      setListOrders(result);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDragDrop = async (
    e: DragEvent<HTMLDivElement>,
    order: RouteWaypointType,
    index: number
  ) => {
    if (!currentOrder?.id) {
      return;
    }

    e.preventDefault();
    e.stopPropagation();

    setIsLoading(true);

    const result = listOrders.map(listItem => {
      if (listItem.orderingIndex === order.orderingIndex) {
        return {
          ...listItem,
          orderingIndex: currentOrder.orderingIndex,
        };
      }

      if (listItem.orderingIndex === currentOrder.orderingIndex) {
        return {
          ...listItem,
          orderingIndex: order.orderingIndex,
        };
      }

      return listItem;
    });

    try {
      await updateRoute({
        ...data,
        waypoints: result.sort(sortOrders),
      });

      setListOrders(result);
    } finally {
      setIsLoading(false);
      setCurrentOrder({} as RouteWaypointType);
    }
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    const isAddressDrag = !!currentOrder?.id;
    if (isAddressDrag) {
      e.preventDefault();
    }
    return isAddressDrag;
  };

  const handleDragStart = (e: DragEvent<HTMLDivElement>, order: RouteWaypointType) => {
    setCurrentOrder(order);
  };

  const handleDragEnd = () => {
    setCurrentOrder({} as RouteWaypointType);
  };

  if (isLoading) {
    return <SpinWrapped />
  }

  return (
    <>
      <Tabs defaultActiveKey="1">
        <TabPane tab='Адреса' key="1">
          <div
            onDragOver={(e) => {
              // не даем RouteDetailed подсветиться / принять drop
              e.stopPropagation();
            }}
            onDrop={(e) => {
              // блокируем drop заявок в область адресов
              e.stopPropagation();
            }}
          >
            <S.List>
              {listOrders?.sort(sortOrders)?.map((point, idx) => (
                <Address
                  key={uuid()}
                  data={data}
                  onDragStart={handleDragStart}
                  onDragOver={handleDragOver}
                  onDrop={handleDragDrop}
                  onDragEnd={handleDragEnd}
                  point={point}
                  handleListItemUp={handleListItemUp}
                  handleListItemDown={handleListItemDown}
                  handleDeleteListItem={handleDeleteListItem}
                  idx={idx}
                  lastIndex={listOrders.length}
                />
              ))}
            </S.List>
          </div>
        </TabPane>
      </Tabs>
    </>
  );
};
