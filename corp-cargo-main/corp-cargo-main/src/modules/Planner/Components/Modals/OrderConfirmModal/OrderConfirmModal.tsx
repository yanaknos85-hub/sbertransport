import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useUpdateRouteV2 } from 'api/planner';
import { Container } from '../Container';
import { ListId } from './ListId';

import * as S from './OrderConfirmModal.style';

interface Props {
  visible: boolean;
  handleCancel: () => void;
}

export const OrderConfirmModal: FC<Props> = observer(props => {
  const { visible, handleCancel } = props;

  const { plannerStore } = useAppStoreContext();

  const {
    checkedOrdersListStore, clearCheckedListStore, draggedOrder, routeStore,
  } = plannerStore;

  const [updateRouteV2] = useUpdateRouteV2();

const handleAddToRoute = async () => {
  // Получаем заявки из обоих источников
  const ordersToAdd = checkedOrdersListStore.length > 0
    ? checkedOrdersListStore
    : (draggedOrder ? [draggedOrder] : []);

  const routeId = routeStore?.id;
  
  if (!routeId) return;

  const payload = {
    routeListId: routeId,
    requests: ordersToAdd.map((order) => order.id),
  };

  await updateRouteV2(payload);
  handleCancel();
  clearCheckedListStore();
};
  const handleReset = () => {
    handleCancel();
    plannerStore.handleResetOrder();
  };

  return (
    <Container
      visible={visible}
      footer={null}
      centered
      width={550}
      onCancel={handleCancel}
    >
      <S.ModalContainer>
        <S.Title>Вы уверены, что хотите добавить заявки в маршрут?</S.Title>
        <S.NameRouteTitle>
          Маршрут{' '}
          {routeStore?.humanReadableId}
        </S.NameRouteTitle>
        <S.SubTitle>В маршрут добавятся новые заявки:</S.SubTitle>
        <S.RouteList>
          {checkedOrdersListStore.length ? <ListId list={checkedOrdersListStore} /> : draggedOrder?.humanReadableId}
        </S.RouteList>
        <S.ButtonsContainer>
          <S.ButtonOk onClick={() => handleAddToRoute()}>Добавить в маршрут</S.ButtonOk>
          <S.ButtonCancel onClick={handleReset}>Отменить</S.ButtonCancel>
        </S.ButtonsContainer>
      </S.ModalContainer>
    </Container>
  );
});
