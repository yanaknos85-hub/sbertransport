import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { usePlanner } from '../../../../context/PlannerContext';
import { AddToRouteModal } from '../../../Modals/AddToRouteModal/AddToRouteModal';
import { OrderConfirmModal } from '../../../Modals/OrderConfirmModal/OrderConfirmModal';
import { NewRouteModal } from '../../../Modals/NewRouteModal/NewRouteModal';
import { OrderWaypointMinimalType } from '../../../../types';
import { OrderControls } from '../../../OrderControls/OrderControls';
import { OrdersList } from '../OrdersList/OrdersList';
import { Buttons } from '../Buttons/Buttons';
import { OrderSwitch } from './OrderSwitch';
import { Pagination } from 'modules/Planner/Pagination/Pagination';
import * as S from './Orders.styles';

interface Props {
  handleChangeListItem: (e: any) => void;
  handleOrderWaypoints?: (waypoints: OrderWaypointMinimalType[], id: string) => void;
  orderId: string;
  isFetchingOrders?: boolean;
  handleOrders: () => void;
  handleRefetchAutoPlanning?: () => void;
}

export const Orders: FC<Props> = observer(props => {
  const {
    handleChangeListItem,
    handleOrderWaypoints,
    orderId,
    isFetchingOrders,
    handleOrders,
    handleRefetchAutoPlanning,
  } = props;

  const [addToRouteVisible, setAddToRouteVisible] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isNewRouteModalVisible, setIsNewRouteModalVisible] = useState(false);

  const {
    handleOpenOrderFilters,
    resetOrdersFilters,
    isRouteVisible,
    isOrderVisible,
    handleSortOrderChange,
  } = usePlanner();

  const { plannerStore } = useAppStoreContext();
  const { ordersListStore } = plannerStore;

  const handleCancelAddToRouteVisible = () => setAddToRouteVisible(false);
  const handleCancelConfirmModal = () => {
    setIsModalVisible(false);
    plannerStore.clearCheckedListStore();
    plannerStore.handleResetOrder();
  };
  const handleCreateRoute = () => setIsNewRouteModalVisible(true);
  const handleCreateRouteCancel = () => setIsNewRouteModalVisible(false);

  return (
    <S.Container full={isOrderVisible}>
      <OrderSwitch handleOrders={handleOrders} isOrderVisible={isOrderVisible} />
      {isOrderVisible && (
        <S.Content>
          <S.OrderListBlock>
            <OrderControls
              resetFilters={resetOrdersFilters}
              handleFilters={handleOpenOrderFilters}
              onSortChange={handleSortOrderChange}
              ordersListStore={ordersListStore ?? { content: [], totalElements: 0, totalPages: 0 }}
              setPagePagination={plannerStore.setOrderPage}
              handleRefetchAutoPlanning={handleRefetchAutoPlanning}
            />
            <OrdersList
              orderId={orderId}
              isRouteVisible={isRouteVisible}
              isFetchingOrders={isFetchingOrders}
              handleChangeListItem={handleChangeListItem}
              handleOrderWaypoints={handleOrderWaypoints}
            />
            <S.PaginationWrapper>
              <Pagination
                pagination={{
                  page: plannerStore.setOrdersListPageSize.page,
                  size: plannerStore.setOrdersListPageSize.size,
                }}
                total={plannerStore.ordersListStore?.totalElements as number}
                setPagination={plannerStore.setOrdersPageSettings}
              />
            </S.PaginationWrapper>
          </S.OrderListBlock>
        </S.Content>
      )}
      {isRouteVisible && !!plannerStore.checkedOrdersListStore.length && (
        <Buttons handleCreateRoute={handleCreateRoute} />
      )}
      <AddToRouteModal
        title="Добавление к маршруту"
        text="Выберите маршрут чтобы добавить заявку"
        visible={addToRouteVisible}
        handleCancel={handleCancelAddToRouteVisible}
        isNew={false}
      />
      <NewRouteModal
        title="Новый маршрут"
        text="Вы создаёте новый маршрут. Уверены?"
        visible={isNewRouteModalVisible}
        handleCancel={handleCreateRouteCancel}
      />
      <OrderConfirmModal visible={isModalVisible} handleCancel={handleCancelConfirmModal} />
    </S.Container>
  );
});
