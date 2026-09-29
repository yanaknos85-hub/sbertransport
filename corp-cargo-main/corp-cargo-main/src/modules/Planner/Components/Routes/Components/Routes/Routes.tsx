import { Button } from '@sber-sbertransport/ui-kit/src';
import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useDeleteRoute, useSendContractorRoute } from 'api/planner';
import { usePlanner } from '../../../../context/PlannerContext';
import { RouteWaypointType } from '../../../../types';
import { Pagination } from 'modules/Planner/Pagination/Pagination';
import { RoutesList } from '../RoutesList/RoutesList';
import { Controls_New } from '../../../Controls_New/Controls_New';
import { OrderConfirmModal } from '../../../Modals/OrderConfirmModal/OrderConfirmModal';
import { Tools_New } from '../../../Tools_New/Tools_New';
import RouteDetailed from '../../../RouteDetailed/RouteDetailed';

import * as S from './Routes.style';

interface Props {
  routeId?: string;
  isAddToRoute?: boolean;
  handleChangeListItem?: (e: any) => void;
  handleWaypoints?: (waypoints: RouteWaypointType[], id: string) => void;
  handleRefetchAutoPlanning?: () => void;
  isFetchingRoutes?: boolean;
  handleUpdateData?: () => void;
  handleDisplayRoutes?: () => void;
}

export const Routes: FC<Props> = observer(props => {
  const {
    routeId,
    handleWaypoints,
    handleRefetchAutoPlanning,
    isFetchingRoutes,
    handleUpdateData,
    handleDisplayRoutes,
  } = props;

  const [isModalVisible, setIsModalVisible] = useState(false);
  const {
    handleOpenRouteFilters,
    resetRoutesFilters,
    handleSortRouteChange,
    checkedListRoutes,
    setCheckedListRoutes,
    isRouteVisible,
    routeMode,
  } = usePlanner();
  const [sendContractorRoute] = useSendContractorRoute();

  const { plannerStore, logger } = useAppStoreContext();
  const [deleteRoute] = useDeleteRoute();

  const { routesListStore } = plannerStore;

  const handleOpenConfirmModal = () => {
    setIsModalVisible(true);
  };

  const handleCancelConfirmModal = () => {
    setIsModalVisible(false);
    plannerStore.handleResetOrder();
    plannerStore.clearRouteStore();
  };


  const sendRoutes = () => {
    checkedListRoutes.forEach(route => {
      sendContractorRoute(route.id)
      .then(() => {
        setCheckedListRoutes([]);
      })
      .catch((e) => {
        logger.toNotify('error',
          e.response.data.message,
          'Ошибка',
          5,
        )
      });
    });
  };

  const deleteRoutes = () => {
    checkedListRoutes.forEach(route => deleteRoute(route.id));
    setCheckedListRoutes([]);
  };

  return (
    <S.Wrapper>
      {routeMode === 'detailed' ? (
        <RouteDetailed />
      ) : (
        <Tools_New handleRoutes={handleDisplayRoutes} />
      )}
      {isRouteVisible && (routeMode === 'list' || routeMode === 'choose') && (
        <S.Container>
          <Controls_New
            resetFilters={resetRoutesFilters}
            handleFilters={handleOpenRouteFilters}
            onSortChange={handleSortRouteChange}
            setPagePagination={plannerStore.setRoutePage}
            routesListStore={routesListStore.content}
            isAddToRoute={routeMode === 'choose'}
            handleRefetchAutoPlanning={handleRefetchAutoPlanning}
            handleUpdateData={handleUpdateData}
          />
          <RoutesList
            routeId={routeId}
            isFetchingRoutes={isFetchingRoutes}
            handleOpenConfirmModal={handleOpenConfirmModal}
            isAddToRoute={routeMode === 'choose'}
            handleWaypoints={handleWaypoints}
          />
          {!!checkedListRoutes?.length && (
            <S.Footer>
              <Button
                onClick={deleteRoutes}
                type="text"
                size="small">
                Удалить
              </Button>
              <Button size="small" onClick={sendRoutes}>Отправить контрагенту</Button>
            </S.Footer>
          )}
            <S.PaginationWrapper>
              <Pagination
                pagination={{
                  page: plannerStore.setRoutesListPageSize.page,
                  size: plannerStore.setRoutesListPageSize.size,
                }}
                total={plannerStore.routesListStore?.totalElements}
                setPagination={plannerStore.setRoutesPageSettings}
              />
            </S.PaginationWrapper>
        </S.Container>
      )}
      <OrderConfirmModal visible={isModalVisible} handleCancel={handleCancelConfirmModal} />
    </S.Wrapper>
  );
});
