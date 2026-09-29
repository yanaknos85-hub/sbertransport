import { ConfigProvider as UIKitProvider, Map2GISContext, use2GIS } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import { BreadcrumbsContext } from 'components/Breadcrumbs/Breadcrumbs';
import AppStore, { AppStoreContext } from 'ioc';
import { TripsProvider } from 'context/Trips.context';
import { AutoparkContext, Props as AutoparkProps } from 'context/Autopark.context';
import { BranchContext, Props as BranchProps } from 'context/Branch.context';
import { ShiftEditApi, ShiftEditContext } from 'context/ShiftEdit.context';
import { ShiftEditLayer } from 'modules/Schedule2.0/ShiftEditLayer';

interface Props {
  value: {
    queryCache: QueryCache;
    breadcrumbs: BreadcrumbsContext;
    mapContext: ReturnType<typeof use2GIS>;
    autopark: AutoparkProps;
    branch: BranchProps;
    shiftEditApi: ShiftEditApi;
  };
  children: React.ReactNode;
}

export const AppProvider: FC<Props> = observer(({ value, children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  return (
    <I18Provider>
      <UIKitProvider>
        <AppStoreContext.Provider value={appStore}>
          <ReactQueryCacheProvider queryCache={value.queryCache}>
            <BreadcrumbsContext.Provider value={value.breadcrumbs}>
              <Map2GISContext.Provider value={value.mapContext}>
                <AutoparkContext.Provider value={value.autopark}>
                  <BranchContext.Provider value={value.branch}>
                    <ShiftEditContext.Provider value={value.shiftEditApi}>
                      <ShiftEditLayer>
                        <TripsProvider>
                          {children}
                        </TripsProvider>
                      </ShiftEditLayer>
                    </ShiftEditContext.Provider>
                  </BranchContext.Provider>
                </AutoparkContext.Provider>
              </Map2GISContext.Provider>
            </BreadcrumbsContext.Provider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
