import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { ConfigProvider as UIKitProvider, Map2GISContext, use2GIS } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import { BreadcrumbsContext } from 'shared/components/Breadcrumbs';
import { UiContextProvider } from 'shared/components/UI/UiContext';
import AppStore, { AppStoreContext } from 'stores';

interface Props {
  value: {
    queryCache: QueryCache;
    breadcrumbs: BreadcrumbsContext;
    mapContext: ReturnType<typeof use2GIS>;
  };
  children: React.ReactNode;
}

export const AppProvider: FC<Props> = observer(({ value, children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  return (
    <UIKitProvider>
      <Map2GISContext.Provider value={value.mapContext}>
        <AppStoreContext.Provider value={appStore}>
          <ReactQueryCacheProvider queryCache={value.queryCache}>
            <UiContextProvider>
              <BreadcrumbsContext.Provider value={value.breadcrumbs}>
                {children}
              </BreadcrumbsContext.Provider>
            </UiContextProvider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </Map2GISContext.Provider>
    </UIKitProvider>
  );
});

export default AppProvider;
