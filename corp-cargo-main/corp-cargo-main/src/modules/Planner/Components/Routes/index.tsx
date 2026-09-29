import React, { useState } from 'react';
import { useGeoPosition } from 'shared/hooks/useGeoPosition';
import * as S from 'modules/Planner/Components/Planner/Planner.styles';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Routes } from './Components/Routes/Routes';
import { PlannerStyled } from '../styles.common';
import { CoordinatesType, RoutesFiltersType, RouteWaypointType } from '../../types';
import { usePlanner } from '../../context/PlannerContext';
import { RoutesFilters } from '../Modals/Filters/RoutesFilters';

// TODO deprecated, проверить!
const EditRoutesList = () => {
  const { position } = useGeoPosition();

  const [listItemId, setListItemId] = useState('');

  const [markers, setMarkers] = useState([] as CoordinatesType[]);

  const { plannerStore } = useAppStoreContext();

  const {
    isVisibleRoutesFilters,
    handleCloseRoutesFilters,
    setRouteFilters,
    defaultSortRoute,
  } = usePlanner();

  const handleWaypoints = (waypoints: RouteWaypointType[], id: string): void => {
    setMarkers(
      waypoints.map(point => ({
        longitude: point.address.longitude,
        latitude: point.address.latitude,
        city: point.address.city,
        street: point.address.street,
        house: point.address.house,
      }))
    );

    setListItemId(id);
  };

  const getDepartmentId = (name?: string) => plannerStore.departmentsListByOrg?.find(({ departmentName }) => departmentName === name)?.id;

  const getFilteredRoutes = (params: RoutesFiltersType) => {
    const {
      regionFrom, regionTo, creationDateRange, desiredDateRange, departmentId,
    } = params;

    const request = {
      ...params,
      regionFrom: regionFrom ? [regionFrom] : [],
      regionTo: regionTo ? [regionTo] : [],
      creationDateRange: {
        start: creationDateRange && new Date(creationDateRange[0]).getTime(),
        end: creationDateRange && new Date(creationDateRange[1]).getTime(),
      },
      desiredDateRange: {
        start: desiredDateRange && new Date(desiredDateRange[0]).getTime(),
        end: desiredDateRange && new Date(desiredDateRange[1]).getTime(),
      },
      authorEmployeeId: plannerStore.employeeId,
      departmentId: getDepartmentId(departmentId),
    };

    setRouteFilters(request);
    handleCloseRoutesFilters();
    defaultSortRoute();
  };

  return (
    <PlannerStyled>
      <Routes
        isAddToRoute
        handleWaypoints={handleWaypoints}
        routeId={listItemId}
      />
      <S.Map
        position={position}
        markers={markers}
        dragging
        locate
        zoomControl
        doubleClickZoom
      />
      <RoutesFilters handleOk={getFilteredRoutes} visible={isVisibleRoutesFilters} />
    </PlannerStyled>
  );
};

export default EditRoutesList;
