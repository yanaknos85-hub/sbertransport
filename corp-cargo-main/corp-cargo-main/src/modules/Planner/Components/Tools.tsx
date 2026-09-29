import React, { FC } from 'react';
import { Switch } from 'antd';
import { TextSwitch } from '../Planner.styles';
import * as S from './styles.common';
import { usePlanner } from '../context/PlannerContext';
import { UpdateData } from './UpdateData/UpdateData';

interface Props {
  handleMap: () => void;
  handleRoutes: () => void;
  handleOrders: () => void;
  isMapVisible: boolean;
  handleUpdateData: () => void;
}
// TODO: deprecated, проверить!
export const Tools: FC<Props> = props => {
  const {
    handleMap, handleRoutes, handleOrders, isMapVisible, handleUpdateData,
  } = props;

  const { isRouteVisible, isOrderVisible } = usePlanner();

  return (
    <S.Container mt={isRouteVisible && isOrderVisible}>
      <div>
        {/* TODO: Скрыто до обновления дизайна
        <TextSwitch>
          <Switch defaultChecked={isMapVisible} onClick={handleMap} />
          <TextSwitch>Карта</TextSwitch>
        </TextSwitch> */}
        <TextSwitch>
          <Switch defaultChecked={isRouteVisible} onClick={handleRoutes} />
          <TextSwitch>Маршруты</TextSwitch>
        </TextSwitch>
        <TextSwitch>
          <Switch defaultChecked={isOrderVisible} onClick={handleOrders} />
          <TextSwitch>Заявки</TextSwitch>
        </TextSwitch>
      </div>
      <UpdateData handleUpdate={handleUpdateData} />
    </S.Container>
  );
};
