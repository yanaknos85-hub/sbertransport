/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, lazy } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Home from 'modules/Home/Home';
import Page404 from 'modules/Page404/Page404';

const PlannerRouter = lazy(() => import ('modules/Planner/PlannerRouter'));

export const AppRouter: FC = (): JSX.Element => {
  return (
    <>
      <Switch>
        <Route path={routes.HOME} component={Home} />
        <Route path={routes.PAGE} component={Home} />
        <Route path={routes.PAGE_404} component={Page404} />

        <Route path={routes.MULTI_LOGISTICS} component={PlannerRouter} />

        <Route exact path={routes.MAIN} component={() => <Redirect to={routes.HOME} />} />
        <Route exact path={routes.EXTERNAL} component={() => <Redirect to={routes.HOME} />} />
        <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
      </Switch>
    </>
  );
};

export default AppRouter;
