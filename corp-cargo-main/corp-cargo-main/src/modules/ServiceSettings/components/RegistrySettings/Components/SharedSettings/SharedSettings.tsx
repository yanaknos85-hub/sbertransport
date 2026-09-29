import React, { ReactNode, Suspense } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import { Locations } from 'modules/Locations/Locations';
import styles from './Shared.module.scss';
import GeoSettings from './GeoSettings/GeoSettings';
import WorkingGroupsSettings from './WorkingGroupsSettings/WorkingGroupsSettings';

const getItems = (): { tab: string; content: ReactNode; disabled?: boolean }[] => [
  {
    tab: 'Корпоративные адреса',
    content: <Locations className={styles.LocationsWrapper} />,
  },
  {
    tab: 'Геозоны',
    content: <GeoSettings />,
  },
  {
    tab: 'Рабочие группы',
    content: <WorkingGroupsSettings />,
  },
];

export const SharedSettings = withErrorBoundary(() => {
  const items = getItems();

  return (
    <Suspense fallback={<SpinWrapped />}>
      <div>
        <Tab className={styles.Tabs} destroyInactiveTabPane>
          {items.map(({ tab, content }) => (
            <TabPane
              tab={tab}
              key={tab}
              theme="card"
            >
              {content}
            </TabPane>
          ))}
        </Tab>
      </div>
    </Suspense>
  );
});
