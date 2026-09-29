import { ConfigProvider as UIKitProvider, Map2GISProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import AppStore, { AppStoreContext } from 'stores';
import { queryCache, ReactQueryCacheProvider } from 'constants/constants';
import { BreadcrumbsProvider } from 'shared/components/Breadcrumbs';
import { API_2GIS_KEY, API_2GIS_SDO_KEY } from 'constants/constants.app';

export const AppProvider: FC = observer(({ children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  return (
    <UIKitProvider>
      <AppStoreContext.Provider value={appStore}>
        <ReactQueryCacheProvider queryCache={queryCache}>
          <BreadcrumbsProvider>
            <Map2GISProvider APIKey={appStore.configStore.env.IS_SDO ? API_2GIS_SDO_KEY : API_2GIS_KEY}>
              {children}
            </Map2GISProvider>
          </BreadcrumbsProvider>
        </ReactQueryCacheProvider>
      </AppStoreContext.Provider>
    </UIKitProvider>
  );
});

export default AppProvider;
