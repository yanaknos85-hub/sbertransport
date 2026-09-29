import React, { useEffect, useState } from 'react';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { observer } from 'mobx-react';
import moment from 'moment';
import { Orders } from 'modules/Planner/Components/Orders/Components/Orders/Orders';
import { Routes } from 'modules/Planner/Components/Routes/Components/Routes/Routes';
import { OrdersFilters } from 'modules/Planner/Components/Modals/Filters/OrdersFilters';
import { RoutePreviewModal } from 'modules/Planner/Components/Modals/RoutePreviewModal/RoutePreviewModal';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { Segment } from 'stores/Geo/Geo.interface';

import {
  OrdersFiltersType,
  RoutesFiltersType,
  RouteWaypointType,
  OrdersListMinimalStoreType,
  OrderListMinimalType,
  OrderWaypointMinimalType,
} from 'modules/Planner/types';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { useRoutesQuery } from './hooks/useRoutesQuery';
import { useOrdersQuery } from './hooks/useOrdersQuery';
import { RoutesFilters } from '../Modals/Filters/RoutesFilters';

import * as S from './Planner.styles';

const Planner = observer(() => {
  const [isPreviewVisible, setIsPreviewVisible] = useState(false);
  const [listItemId, setListItemId] = useState('');

  const { plannerStore } = useAppStoreContext();

  const {
    isVisibleRoutesFilters,
    isVisibleOrdersFilters,
    handleCloseRoutesFilters,
    handleCloseOrdersFilters,
    setRouteFilters,
    setOrderFilters,
    isRouteVisible,
    setIsRouteVisible,
    isOrderVisible,
    setIsOrderVisible,
    defaultSortRoute,
    coordinates,
    setMarkers,
    markers,
  } = usePlanner();

  const mapPolylines = coordinates.length > 0 ? [{ coordinates }] : undefined;

  const {
    data: routesList,
    refetch: refetchRoutes,
    isFetching: isFetchingRoutes
  } = useRoutesQuery();

  const {
    data: ordersList,
    refetch: refetchOrders,
    isFetching: isFetchingOrders
  } = useOrdersQuery();

  const handleRefetchAutoPlanning = () => {
    refetchRoutes();
    refetchOrders();
  };

  useEffect(() => {
    if (routesList) {
      plannerStore.saveRoutesToStore(routesList);
    }
  }, [routesList]);

  useEffect(() => {
    if (ordersList) {
      const minimalStore: OrdersListMinimalStoreType = {
        content: ordersList.content as OrderListMinimalType[],
        totalElements: ordersList.totalElements,
        totalPages: ordersList.totalPages,
      };

      plannerStore.saveOrdersToStore(minimalStore);
    }
  }, [ordersList]);

  const handleUpdateData = () => {
    refetchRoutes();
    refetchOrders();
  };

  const handleDisplayRoutes = () => {
    setIsRouteVisible(routeState => !routeState);
    plannerStore.clearCheckedListStore();
    setIsPreviewVisible(!!plannerStore.checkedOrdersListStore.length && !isOrderVisible && !isRouteVisible);
  };

  const handleDisplayOrders = () => {
    setIsOrderVisible(prevState => !prevState);
    plannerStore.clearCheckedListStore();
  };

  const handleClosePreviewModal = () => {
    plannerStore.clearCheckedListStore();
    setIsPreviewVisible(false);
  };

  const handleChangeListItem = (e: CheckboxChangeEvent) => {
    const order = plannerStore.ordersListStore?.content.find(item => item.id === e.target.value);

    if (e.target.checked && order !== undefined) {
      plannerStore.addOrderToRoute(order);
    } else {
      plannerStore.removeOrderRoute(e.target.value);
    }

    const isVisible =
      (!!plannerStore.checkedOrdersListStore.length && isOrderVisible && !isRouteVisible) ||
      (!!plannerStore.checkedOrdersListStore.length && !isOrderVisible && isRouteVisible);

    if (plannerStore.checkedOrdersListStore) {
      setIsPreviewVisible(isVisible);
    }
  };

  const getDepartmentId = (name?: string) => plannerStore.departmentsListByOrg?.find(({ departmentName }) => departmentName === name)?.id;

  const getFilteredRoutes = (params: RoutesFiltersType) => {
    const { regionFrom, regionTo, creationDateRange, desiredDateRange, departmentId, statusSet } = params;
    const request = {
      ...params,
      regionFrom: regionFrom || [],
      regionTo: regionTo || [],
      statusSet: statusSet ? [statusSet] : [],
      creationDateRange: {
        start: creationDateRange && moment(creationDateRange[0]).utc().startOf('day').toISOString(),
        end: creationDateRange && moment(creationDateRange[1]).utc().endOf('day').toISOString(),
      },
      desiredDateRange: {
        start: desiredDateRange && moment(desiredDateRange[0]).utc().startOf('day').toISOString(),
        end: desiredDateRange && moment(desiredDateRange[1]).utc().endOf('day').toISOString(),
      },
    };
    setRouteFilters(request);
    handleCloseRoutesFilters();
    defaultSortRoute();
  };

  const getFilteredOrders = (params: OrdersFiltersType) => {
    const { regionFrom, regionTo, creationDateRange, desiredDateRange, departmentEmployeeId, departmentSenderId, departmentOrderId, departmentId } = params;
    const request = {
      ...params,
      regionFrom: regionFrom || [],
      regionTo: regionTo || [],
      creationDateRange: {
        start: creationDateRange && moment(creationDateRange[0]).utc().startOf('day').toISOString(),
        end: creationDateRange && moment(creationDateRange[1]).utc().endOf('day').toISOString(),
      },
      desiredDateRange: {
        start: desiredDateRange && moment(desiredDateRange[0]).utc().startOf('day').toISOString(),
        end: desiredDateRange && moment(desiredDateRange[1]).utc().endOf('day').toISOString(),
      },
      departmentEmployeeId: getDepartmentId(departmentEmployeeId),
      departmentSenderId: getDepartmentId(departmentSenderId),
      departmentOrderId: getDepartmentId(departmentOrderId),
      authorEmployeeId: plannerStore.employeeId,
      departmentId: getDepartmentId(departmentId),
    };
    setOrderFilters(request);
    handleCloseOrdersFilters();
  };

  const handleWaypoints = (waypoints: RouteWaypointType[], id: string): void => {
    setMarkers(waypoints.map(point => ({
      longitude: point.address.longitude,
      latitude: point.address.latitude,
      city: point.address.city,
      street: point.address.street,
      house: point.address.house,
    })));
    setListItemId(id);
  };

  const handleOrderWaypoints = (waypoints: OrderWaypointMinimalType[], id: string): void => {
    setMarkers(
      waypoints.map(point => ({
        longitude: point.longitude,
        latitude: point.latitude,
        city: point.addressStringRepresentation,
      }))
    );
    setListItemId(id);
  };

  return (
    <>
      <S.PlannerStyled>
        <S.MainStyled>
          <Routes
            handleChangeListItem={handleChangeListItem}
            handleWaypoints={handleWaypoints}
            routeId={listItemId}
            handleRefetchAutoPlanning={handleRefetchAutoPlanning}
            isFetchingRoutes={isFetchingRoutes}
            handleUpdateData={handleUpdateData}
            handleDisplayRoutes={handleDisplayRoutes}
          />
          <Orders
            handleChangeListItem={handleChangeListItem}
            handleOrderWaypoints={handleOrderWaypoints}
            orderId={listItemId}
            handleRefetchAutoPlanning={handleRefetchAutoPlanning}
            isFetchingOrders={isFetchingOrders}
            handleOrders={handleDisplayOrders}
          />
          <MapComponent
            markers={markers as WaypointModel[]}
            polylines={mapPolylines as Segment[]}
            dragging
            zoomControl
            zoomControlPosition={isOrderVisible ? 'bottomCenter' : 'bottomRight'}
            fitToShowAllGeometry
          />
        </S.MainStyled>
        <RoutePreviewModal
          visible={isPreviewVisible}
          checkedOrders={plannerStore.checkedOrdersListStore}
          onCancel={handleClosePreviewModal}
        />
      </S.PlannerStyled>
      <OrdersFilters handleOk={getFilteredOrders} visible={isVisibleOrdersFilters} />
      <RoutesFilters handleOk={getFilteredRoutes} visible={isVisibleRoutesFilters} />
    </>
  );
});

export default Planner;
