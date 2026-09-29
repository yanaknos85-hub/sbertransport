import React, { FC, useEffect } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import Delegates from './DelegatesPage/Delegates';
import { ModalProvider } from './DelegatesPage/context/modal.context';

const DelegatesRouter: FC = observer(() => {
  const match = useRouteMatch();

  const {
    [StoreNames.delegatesStore]: delegatesStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
  } = useAppStoreContext();

  useEffect(() => {
    transportTypesStore.initStore();
  }, [delegatesStore, transportTypesStore]);

  return (
    <ModalProvider>
      <Switch>
        <Route
          path={match.path}
          component={Delegates}
          exact
        />
      </Switch>
    </ModalProvider>
  );
});

export default DelegatesRouter;
