import React, { FC, lazy } from 'react';
import { Route, Switch, Redirect, useLocation } from 'react-router-dom';
import { PlanerProvider } from 'modules/Planner/context/PlannerContext';
import * as routes from 'constants/constants.routes';
import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';

const MPlanner = lazy(() => import('modules/Planner/Components/Main'));
const RouteDetailed = lazy(() => import('modules/Planner/Components/RouteDetailed/RouteDetailed'));
const EditRoutesList = lazy(() => import('modules/Planner/Components/Routes'));
const OrderDetailed = lazy(() => import('modules/Planner/Components/OrderDetailed/OrderDetailed'));
const MShowOrder = lazy(() => import('modules/Planner/Components/Monitor/DetailedCard/ShowOrder'));

export const Router: FC = (): JSX.Element => { 
  const location = useLocation();
  const isJournalRoute = location.pathname.includes(routes.PLANNER_JOURNAL_DETAILED);

  return (
    <PlanerProvider>
      <Switch>
        <Route exact path={routes.MULTI_LOGISTICS} component={MPlanner} />
        <CustomRoute path={routes.MULTI_LOGISTICS} bc={isJournalRoute ? "Журнал маршрутов" : "Планировщик маршрутов"}>
          <Switch>
            <CustomRoute path={routes.PLANNER_ROUTE_CREATE} component={RouteDetailed} bc="Создание маршрута" />
            <CustomRoute path={routes.PLANNER_ROUTE_LIST} component={EditRoutesList} bc="Список маршрутов" />
            <CustomRoute path={`${routes.PLANNER_ROUTE_DETAILED}/:id`} component={RouteDetailed} bc="Просмотр маршрута" />
            <CustomRoute path={`${routes.PLANNER_ORDER_DETAILED}/:id`} component={OrderDetailed} bc="Просмотр заявки" />
            <CustomRoute path={`${routes.PLANNER_JOURNAL_DETAILED}/:id`} component={MShowOrder} bc="Детальный просмотр маршрута" />
            <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
          </Switch>
        </CustomRoute>
      </Switch>
    </PlanerProvider>
  );
};

export default Router;