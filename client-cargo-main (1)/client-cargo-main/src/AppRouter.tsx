
import React, { FC } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { CustomRoute, CustomSwitch, MaybeBreadcrumbs } from 'shared/components/Breadcrumbs';

import { IS_REMOTE } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import ApprovalRouter from 'modules/ApprovalPage/ApprovalRouter';
import CargoMassMultiple from 'modules/CargoMassMultiple/CargoMassMultiple';
import { CreateMultipleCargoRequest } from 'modules/CargoMultiple';
import RegularCargosRouterMultiple from 'modules/CargosMultiple/CargosRegularRouter';
import CargosMultipleRouter from 'modules/CargosMultiple/CargosRouter';

import ExchangeRouter from './modules/Exchange/ExchageRouter';

export const AppRouter: FC = observer((): JSX.Element => {
  return (
    <>
      <MaybeBreadcrumbs />
      <Switch>
        {/* Журналы */}
        <Route path={routes.CARGOS} component={CargosMultipleRouter} />
        <Route path={routes.REGULAR_CARGOS} component={RegularCargosRouterMultiple} />
        {/* Создание заявки */}
        <Route path={routes.MASS_CARGO_CREATE}>
          <CargoMassMultiple />
        </Route>
        {/* Согласования */}
        <Route path={routes.APPROVEMENT} component={ApprovalRouter} />
        {/* Биржа */}
        <Route path={routes.EXCHANGE} component={ExchangeRouter} />
        {/* Создать заявку */}
        <CustomRoute path="/" bc="Заказать">
          <CustomSwitch>
            <CustomRoute
              bc="Доставка"
              path={routes.CARGO_CREATE}
              component={CreateMultipleCargoRequest}
            />
            <CustomRoute
              path="*"
              component={(): JSX.Element => <Redirect to={!IS_REMOTE ? routes.CARGO_CREATE : '/'} />}
            />
          </CustomSwitch>
        </CustomRoute>
      </Switch>
    </>
  );
});

export default AppRouter;
