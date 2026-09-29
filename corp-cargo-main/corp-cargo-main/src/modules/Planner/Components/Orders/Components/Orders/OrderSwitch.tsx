import React, { FC } from 'react';
import { Switch } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { TextSwitch } from 'modules/Planner/Planner.styles';

import * as S from '../../../styles.common'

interface Props {
  handleOrders: () => void;
  isOrderVisible : boolean
}

export const OrderSwitch: FC<Props> = props => {
  const {
     handleOrders,
  } = props;
  const { plannerStore } = useAppStoreContext();
  const { ordersListStore } = plannerStore;

  const { isOrderVisible: isOrderVisibleFromContext } = usePlanner();

  return (
    <S.Container isOrderVisible={isOrderVisibleFromContext}>
        <TextSwitch>
          <TextSwitch>{`Заявки (${ordersListStore?.totalElements || 0})`}</TextSwitch>
          <Switch defaultChecked={isOrderVisibleFromContext} onClick={handleOrders} />
        </TextSwitch>
    </S.Container>
  );
};
