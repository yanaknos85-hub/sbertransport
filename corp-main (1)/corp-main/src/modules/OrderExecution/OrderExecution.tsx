/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, Suspense, useState } from 'react';
import { observer } from 'mobx-react';
import { Tabs } from 'antd';
import { useHistory, useParams } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import mfLoader from 'mf/MFLoader';
import { Tab } from './constants/tabs.constants';
import TabCard from './components/TabCard';
import * as routes from 'constants/constants.routes';
import styles from './styles.module.scss';

const Passengers = React.lazy(() => mfLoader(import('passengers/modules/OrderExecution')));
const Cargo = React.lazy(() => mfLoader(import('cargo/modules/OrderExecution')));
const Fleet = React.lazy(() => mfLoader(import('fleet/modules/OrderExecution')));

const OrderExecution: FC = observer(() => {
  const { service } = useParams();
  const [activeKey, setActiveKey] = useState(`${routes.ORDER_EXECUTION}/${service}`);
  const { replace } = useHistory();

  const changeActiveTabHandler = (activeKey: string) => {
    setActiveKey(activeKey);
    replace(activeKey);
  };

  return (
    <div className={styles.orderExecution}>
      <Tabs
        defaultActiveKey={activeKey}
        activeKey={activeKey}
        onChange={activeKey => changeActiveTabHandler(activeKey as Tab)}
      >
        <Tabs.TabPane key={routes.ORDER_EXECUTION_PASSENGERS} tab={<TabCard tabKey={Tab.passengers} />}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <Passengers />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
        <Tabs.TabPane key={routes.ORDER_EXECUTION_CARGO} tab={<TabCard tabKey={Tab.cargo} />}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <Cargo />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
        <Tabs.TabPane key={routes.ORDER_EXECUTION_CAR_SERVICE} tab={<TabCard tabKey={Tab.carService} />}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <Fleet />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
        <Tabs.TabPane key={routes.ORDER_EXECUTION_PARKING} tab={<TabCard tabKey={Tab.parking} />} disabled />
      </Tabs>
    </div>
  );
});

export default OrderExecution;
