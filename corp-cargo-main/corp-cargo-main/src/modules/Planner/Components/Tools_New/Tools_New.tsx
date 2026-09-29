import React, { FC } from 'react';
import { Switch } from 'antd';
import { usePlanner } from '../../context/PlannerContext';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as S from './Tools_New.styles';

interface Props {
  handleRoutes?: () => void;
}

export const Tools_New: FC<Props> = props => {
  const { handleRoutes } = props;

  const { plannerStore } = useAppStoreContext();

  const { routesListStore } = plannerStore;

  const { isRouteVisible } = usePlanner();

  const numberRoutes = routesListStore.totalElements || 0;

  return (
    <S.Container isRouteVisible={isRouteVisible}>
        <S.TextSwitch>Маршруты {`(${numberRoutes})`}</S.TextSwitch>
        <Switch defaultChecked={isRouteVisible} onClick={() => handleRoutes?.()} />
    </S.Container>
  );
};
