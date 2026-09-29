import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';

import { SortProperty } from 'components/SortMenu/SortConstants';
import { CoordinatesType, MarkersType, RouteMode, RouteType, MonitorFiltersType } from '../types';

const useHook = () => {
  const [isVisibleRoutesFilters, setVisibleRoutesFilters] = useState(false);
  const [isVisibleMonitorFilters, setVisibleMonitorFilters] = useState(false);
  const [isVisibleOrdersFilters, setVisibleOrdersFilters] = useState(false);
  const [routeFilters, setRouteFilters] = useState({});
  const [monitorFilters, setMonitorFilters] = useState<MonitorFiltersType>({});
  const [orderFilters, setOrderFilters] = useState({});
  const [isRouteVisible, setIsRouteVisible] = useState(true);
  const [routeMode, setRouteMode] = useState<RouteMode>('list');
  const [isOrderVisible, setIsOrderVisible] = useState(true);
  const [isAddToRoute, setIsAddToRoute] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [checkedListRoutes, setCheckedListRoutes] = useState<RouteType[]>([]);
  const [directionRouteAsc, setDirectionRouteAsc] = useState(false);
  const [directionOrderAsc, setDirectionOrderAsc] = useState(false);
  const [sortingRouteProperty, setSortingRouteProperty] = useState(SortProperty.DESIRED_DATE as string);
  const [sortingOrderProperty, setSortingOrderProperty] = useState(SortProperty.DESIRED_DATE as string);

  const handleOpenRouteFilters = () => {
    setVisibleRoutesFilters(prevState => !prevState);
  };

  const handleCloseRoutesFilters = () => {
    setVisibleRoutesFilters(false);
  };

  const handleOpenMonitorFilters = () => {
    setVisibleMonitorFilters(prevState => !prevState);
  };

  const handleCloseMonitorFilters = () => {
    setVisibleMonitorFilters(false);
  };

  const handleOpenOrderFilters = () => {
    setVisibleOrdersFilters(prevState => !prevState);
  };

  const handleCloseOrdersFilters = () => {
    setVisibleOrdersFilters(false);
  };

  const resetRoutesFilters = () => {
    setRouteFilters({});
  };

  const resetOrdersFilters = () => {
    setOrderFilters({});
  };

  const handleAddToRoute = () => {
    setIsOrderVisible(false);
    setIsRouteVisible(true);
    setIsAddToRoute(true);
  };

  const handleSortRouteChange = useCallback(
    (directionAsc: boolean, sortingProperty: string) => {
      setDirectionRouteAsc(directionAsc);
      setSortingRouteProperty(sortingProperty);
    },
    [directionRouteAsc, sortingRouteProperty]
  );

  const defaultSortRoute = () => {
    setDirectionRouteAsc(false);
    setSortingRouteProperty(SortProperty.DESIRED_DATE);
  };

  const handleSortOrderChange = useCallback(
    (directionAsc: boolean, sortingProperty: string) => {
      setDirectionOrderAsc(directionAsc);
      setSortingOrderProperty(sortingProperty);
    },
    [directionOrderAsc, sortingOrderProperty]
  );

  const [routeIdDetailed, setRouteIdDetailed] = useState('');

  const [coordinates, setCoordinates] = useState<CoordinatesType[]>([]);
  const [markers, setMarkers] = useState<MarkersType[]>([])

  return {
    isVisibleRoutesFilters,
    isVisibleOrdersFilters,
    isVisibleMonitorFilters,
    handleOpenRouteFilters,
    handleOpenOrderFilters,
    handleCloseRoutesFilters,
    handleCloseOrdersFilters,
    handleOpenMonitorFilters,
    handleCloseMonitorFilters,
    resetRoutesFilters,
    resetOrdersFilters,
    routeFilters,
    setRouteFilters,
    monitorFilters,
    setMonitorFilters,
    orderFilters,
    setOrderFilters,
    isRouteVisible,
    setIsRouteVisible,
    isOrderVisible,
    setIsOrderVisible,
    isAddToRoute,
    setIsAddToRoute,
    handleAddToRoute,
    isModalVisible,
    setIsModalVisible,
    checkedListRoutes,
    setCheckedListRoutes,
    directionRouteAsc,
    directionOrderAsc,
    sortingRouteProperty,
    sortingOrderProperty,
    handleSortRouteChange,
    handleSortOrderChange,
    defaultSortRoute,
    routeIdDetailed,
    setRouteIdDetailed,
    coordinates,
    setCoordinates,
    markers,
    setMarkers,
    routeMode,
    setRouteMode,
  };
};

export const [usePlanner, PlanerProvider] = createCallableCtx(useHook, { name: 'PlanerProvider' });
