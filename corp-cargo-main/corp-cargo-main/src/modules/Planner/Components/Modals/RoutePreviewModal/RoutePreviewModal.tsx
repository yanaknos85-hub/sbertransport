import React, { FC, useState } from 'react';
import { Divider, Space, Tabs } from 'antd';
import { Link } from 'react-router-dom';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useTranslation } from 'i18n';
import { getRoundedParams } from 'utils/getRoundedParams';
import { ReactComponent as Cross } from '../../../images/crossIcon.svg';
import { OrderListMinimalType } from '../../../types';
import { NewRouteModal } from '../NewRouteModal/NewRouteModal';
import * as S from './RoutePreviewModal.style';
import { OrderParams } from './Components/OrderParams/OrderParams';
import { AddressesList } from './Components/AddressesList/AddressesList';
import { IdsList } from './Components/IdsList/IdsList';

const { TabPane } = Tabs;

interface Props {
  visible: boolean;
  checkedOrders: OrderListMinimalType[];
  onCancel: () => void;
}

export const RoutePreviewModal: FC<Props> = observer(props => {
  const {
    visible, checkedOrders, onCancel,
  } = props;

  const [isNewRouteModalVisible, setIsNewRouteModalVisible] = useState(false);

  const { plannerStore } = useAppStoreContext();

  const { t } = useTranslation();

  const totalParams = plannerStore.checkedOrdersListStore.reduce(
    (acc, { weight, volume, cost }) => {
      acc.weight = acc.weight += weight;
      acc.volume = acc.volume += volume;
      acc.cost = acc.cost += cost;
      return acc;
    },
    {
      weight: 0, volume: 0, cost: 0,
    }
  );

  const { weight: weightR, volume: volumeR } = getRoundedParams({
    weight: totalParams.weight,
    volume: totalParams.volume,
  });

  const handleCreateRoute = () => {
    setIsNewRouteModalVisible(true);
  };

  const handleCreateRouteCancel = () => {
    setIsNewRouteModalVisible(false);
  };

  return (
    <S.Container visible={visible}>
      <S.Wrapper>
        <S.Cross>
          <Cross onClick={onCancel} />
        </S.Cross>
        <Tabs defaultActiveKey="1">
          <TabPane tab={t.Planner.information} key="1">
            <S.Content>
              <IdsList />
              <Space>
                <Link to="logistics/planner/routesList">
                  <S.ButtonOne>{t.Planner.toRoute}</S.ButtonOne>
                </Link>
                <S.ButtonTwo onClick={handleCreateRoute}>{t.Planner.planning}</S.ButtonTwo>
              </Space>
            </S.Content>
          </TabPane>
          <TabPane tab={t.Planner.addresses} key="2">
            <AddressesList checkedOrders={checkedOrders} />
          </TabPane>
        </Tabs>
        <Divider style={{ margin: '14px 0' }} />
        <OrderParams
          weightR={weightR}
          volumeR={volumeR}
          cost={totalParams.cost}
        />
      </S.Wrapper>
      <NewRouteModal
        title={t.Planner.newRoute}
        text={t.Planner.newRouteConfirmation}
        visible={isNewRouteModalVisible}
        handleCancel={handleCreateRouteCancel}
      />
    </S.Container>
  );
});
