import React, { FC } from 'react';
import { Spin } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { NotFoundData } from '../../../NotFoundData';
import { RouteWaypointType } from '../../../../types';
import { RouteListItem_New } from '../RouteListItem_New/RouteListItem_New';
import { RouteType } from 'modules/Planner/types';

import * as S from './RoutesList.styles';

interface Props {
  routeId?: string;
  isAddToRoute?: boolean;
  isFetchingRoutes?: boolean;
  handleOpenConfirmModal?: () => void;
  handleWaypoints?: (waypoints: RouteWaypointType[], id: string) => void;
}

export const RoutesList: FC<Props> = props => {
  const {
    routeId,
    isAddToRoute,
    isFetchingRoutes,
    handleOpenConfirmModal,
    handleWaypoints,
  } = props;

  const { plannerStore } = useAppStoreContext();

  const { routesListStore } = plannerStore;

  return (
    <S.ListWrapper>
      {isFetchingRoutes ? (
        <Spin />
      ) : routesListStore?.content?.length ? (
        routesListStore?.content.map((route: RouteType) => (
          <RouteListItem_New
            key={route.id}
            handleOpenConfirmModal={handleOpenConfirmModal}
            isAddToRoute={isAddToRoute}
            {...route}
            handleWaypoints={handleWaypoints}
            active={route.id === routeId}
          />
        ))
      ) : (
        <NotFoundData />
      )}
    </S.ListWrapper>
  );
};
