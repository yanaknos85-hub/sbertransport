import React, { FC, Suspense, useState } from 'react';
import { useHistory, useLocation } from 'react-router-dom';

import { Tab } from 'shared/components/Tab';
import { TabPane } from 'shared/components/Tab/TabPane';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Fields } from './Organizations/Search';
import { OrganizationsSearch } from './Organizations/OrganizationSearch';
import { OrganizationsGroup } from './OrganizationsGroups/OrganizationGroup';
import * as routes from 'constants/constants.routes';

import styles from './organizations.module.scss';

const Organizations: FC = () => {
  const [searchQuery, setSearchQuery] = useState<Fields>({ page: 0, size: 20 });
  const history = useHistory();
  const location = useLocation();

  const defaultActiveKey = location.pathname.includes(routes.ORGANIZATIONS_GROUPS)
    ? routes.ORGANIZATIONS_GROUPS
    : routes.ORGANIZATIONS;

  return (
    <div className={styles.container} data-name="passenger_dir_organizations">
      <Tab
        themeSize="l"
        defaultActiveKey={defaultActiveKey}
        onChange={route => history.push(route)}
      >
        <TabPane
          themeSize="l"
          tab="Организации"
          key={routes.ORGANIZATIONS}
        >
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <OrganizationsSearch searchQuery={searchQuery} setSearchQuery={setSearchQuery} />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
        <TabPane
          themeSize="l"
          tab="Группы организаций"
          key={routes.ORGANIZATIONS_GROUPS}
        >
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <OrganizationsGroup />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
      </Tab>
    </div>
  );
};

export default Organizations;
