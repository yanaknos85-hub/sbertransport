import { observer } from 'mobx-react';
import React, { Suspense } from 'react';
import { Route, Switch } from 'react-router-dom';
import moment from 'moment';
import 'moment/locale/ru';
import 'reflect-metadata';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import * as routes from 'constants/routes.constants';
import Main from './Main';

moment().locale('ru');

const App = observer((): JSX.Element => {
  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <Route path={routes.APP} component={Main} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default App;
