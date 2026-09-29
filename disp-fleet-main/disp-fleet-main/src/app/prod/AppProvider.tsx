import { ConfigProvider as UIKitProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import { BreadcrumbsContext } from 'components/Breadcrumbs/Breadcrumbs';
import AppStore, { AppStoreContext } from 'ioc';
import { AutoparkContext, Props as AutoparkProps } from 'context/Autopark.context';
import { BranchContext, Props as BranchProps } from 'context/Branch.context';
import { ShiftEditApi, ShiftEditContext } from 'context/ShiftEdit.context';

interface Props {
  value: {
    queryCache: QueryCache;
    breadcrumbs: BreadcrumbsContext;
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
              <AutoparkContext.Provider value={value.autopark}>
                <BranchContext.Provider value={value.branch}>
                  <ShiftEditContext.Provider value={value.shiftEditApi}>
                    {children}
                  </ShiftEditContext.Provider>
                </BranchContext.Provider>
              </AutoparkContext.Provider>
            </BreadcrumbsContext.Provider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
