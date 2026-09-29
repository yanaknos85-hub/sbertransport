import React, { FC } from 'react';
import moment from 'moment';
import { useCreateRoute } from 'api/planner';
import { useProfile } from 'api/profile';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Container } from '../Container';
import { OrderListMinimalType } from '../../../types';
import { mockedAuto } from '../../../mockedData';
import { usePlanner } from '../../../context/PlannerContext';
import { DATE_FORMAT } from 'constants/constants.app';

import * as S from './NewRouteModal.styles';

interface Props {
  title?: string;
  text?: string;
  visible: boolean;
  handleCancel: () => void;
  handleCreate?: () => void;
}

export const NewRouteModal: FC<Props> = props => {
  const {
    title, text, visible, handleCancel,
  } = props;

  const { plannerStore } = useAppStoreContext();

  const [createRoute] = useCreateRoute();
  const profile = useProfile().data;
  const { setRouteIdDetailed, setRouteMode } = usePlanner();

  const handleCreate = async () => {
    const requestObject = {
      author: profile,
      desiredDate: moment.max(plannerStore.checkedOrdersListStore.map(order => moment(order.desiredDate))).format(DATE_FORMAT.DATE_WITH_TIME_ISO_SECONDS),
      startDate: moment.min(plannerStore.checkedOrdersListStore.map(order => moment(order.desiredDate))).format(DATE_FORMAT.DATE_WITH_TIME_ISO_SECONDS),
      transportType: 'DEDICATED',
      tariffId: plannerStore.checkedOrdersListStore[0].tariffId,
      contragentId: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
      auto: mockedAuto,
      addRequests: [...plannerStore.checkedOrdersListStore.map((order: OrderListMinimalType) => order.id)],
    };
    const newRoute = await createRoute(requestObject).catch(() => null);

    if (newRoute) {
      setRouteIdDetailed(newRoute.id)
      setRouteMode('detailed');
      plannerStore.clearStores();
    }

    plannerStore.clearStores();
    handleCancel();
  };

  return (
    <Container
      visible={visible}
      footer={null}
      centered
      width={440}
      onCancel={handleCancel}
    >
      <S.ModalContainer>
        <S.Title>{title}</S.Title>
        <S.Text>{text}</S.Text>
        <S.ButtonsBlock>
          <S.ButtonCancel onClick={handleCancel}>Отменить</S.ButtonCancel>
          <S.ButtonCreate onClick={handleCreate}>Создать</S.ButtonCreate>
        </S.ButtonsBlock>
      </S.ModalContainer>
    </Container>
  );
};
