import { observer } from 'mobx-react';
import React, { Suspense } from 'react';
import { Route, Switch } from 'react-router-dom';
import moment from 'moment';
import 'moment/locale/ru';
import 'reflect-metadata';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import * as routes from 'constants/constants.routes';
import Main from './Main';

moment().locale('ru');

const App = observer((): JSX.Element => {
  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <Route path={routes.MAIN} component={Main} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default App;
