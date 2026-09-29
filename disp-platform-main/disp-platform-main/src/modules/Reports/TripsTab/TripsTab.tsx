import Panel from 'components/Panel/Panel';
import { useTab } from 'hooks/useTab';
import React, { FC, Suspense } from 'react';
import { TripTypes } from './constants/TripsTab.constants';
import { Tab } from 'components/Tab';
import { TabPane } from 'components/Tab/TabPane';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import Passenger from './Passenger';
import { TripsTabQueryProvider } from './context/TripsTab.queryContext';

const TripsTab: FC = () => {
  const [tab, setTab] = useTab<TripTypes>('type');

  return (
    <Panel>
      <Tab
        activeKey={tab}
        onChange={setTab}
        size="small"
        destroyInactiveTabPane
      >
        <TabPane tab="Пассажирские перевозки" key={TripTypes.Passenger}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <TripsTabQueryProvider>
                <Passenger />
              </TripsTabQueryProvider>
            </Suspense>
          </ErrorBoundary>
        </TabPane>

        {/* Скрыто до реализации по просьбе ВП */}
        {/* <TabPane
          tab="Грузовые перевозки"
          key={TripTypes.Cargo}
          disabled
        >
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>

            </Suspense>
          </ErrorBoundary>
        </TabPane> */}
      </Tab>
    </Panel>
  );
};

export default TripsTab;
