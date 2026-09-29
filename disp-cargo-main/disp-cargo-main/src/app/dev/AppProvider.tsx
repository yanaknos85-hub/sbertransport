import { Map2GISProvider, ConfigProvider as UIKitProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import AppStore, { AppStoreContext } from 'ioc';
import { BreadcrumbsProvider } from 'components/Breadcrumbs/Breadcrumbs';
import { API_2GIS_KEY, API_2GIS_SDO_KEY } from 'constants/app.constants';

const queryCache = new QueryCache({
  defaultConfig: {
    queries: {
      suspense: true,
      staleTime: Infinity,
    },
    mutations: {
      throwOnError: true,
    },
  },
});

export const AppProvider: FC = observer(({ children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  const { IS_SDO } = appStore.configStore.env;

  return (
    <I18Provider>
      <UIKitProvider>
        <AppStoreContext.Provider value={appStore}>
          <ReactQueryCacheProvider queryCache={queryCache}>
            <BreadcrumbsProvider>
              <Map2GISProvider APIKey={IS_SDO ? API_2GIS_SDO_KEY : API_2GIS_KEY}>
                {children}
              </Map2GISProvider>
            </BreadcrumbsProvider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
