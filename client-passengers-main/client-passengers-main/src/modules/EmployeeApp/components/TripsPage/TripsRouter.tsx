import React from 'react';
import type { FC } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { IS_REMOTE } from 'constants/constants.env';

import TripRequestDetailedView from './TripRequestDetailedView/TripRequestDetailedView';
import TripsPage from './TripsPage';
import { EmployeeAppLinksTitles } from '../../EmployeeApp.constants';
import TransportOrder from '../CreateTripRequest/Components/TransportOrder';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { CustomRoute, CustomSwitch } from 'shared/components/Breadcrumbs/CustomRoutes';
import { TripsYandexPage } from '../TripsYandexPage/TripsYandexPage';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

// переделать роуты и навигацию когда будет доработан виджет коротких путей для всех транспортов
const TripsRouter: FC = () => {
  const { isMobile } = usePlatformDetect();
  return (
    <Switch>
      <Route
        path={routes.TRIPS_JOURNAL}
        component={TripsPage}
        exact
      />
      <Route
        path={routes.TRIPS_DETAILED}
        component={TripRequestDetailedView}
        exact
      />
      {isMobile && (
      <Route
        path={routes.TRIPS_YANDEX_JOURNAL}
        component={TripsYandexPage}
        exact
      />
      )}

      <CustomRoute path="/" bc="Главная">
        <CustomSwitch>
          <CustomRoute
            path={routes.TRANSPORT_2_0_CREATE}
            bc="Заказать Поездку"
            render={() => <TransportOrder transportType={TransportTypeEnum.TAXI} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_TAXI}`}
            bc={EmployeeAppLinksTitles.taxi}
            render={() => <TransportOrder transportType={TransportTypeEnum.TAXI} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_YANDEX_TAXI}`}
            bc={EmployeeAppLinksTitles.orderYandex}
            render={() => <TransportOrder transportType={TransportTypeEnum.YANDEX} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_PERSONAL}`}
            bc={EmployeeAppLinksTitles.personal}
            render={() => <TransportOrder transportType={TransportTypeEnum.PERSONAL} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_PUBLIC}`}
            bc={EmployeeAppLinksTitles.public}
            render={() => <TransportOrder transportType={TransportTypeEnum.PUBLIC} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_CARSHARING}`}
            bc={EmployeeAppLinksTitles.carsharing}
            render={() => <TransportOrder transportType={TransportTypeEnum.CARSHARING} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_TRANSFER}`}
            bc={EmployeeAppLinksTitles.transfer}
            render={() => <TransportOrder transportType={TransportTypeEnum.GROUP_TRANSFER} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_BUS}`}
            bc={EmployeeAppLinksTitles.bus}
            render={() => <TransportOrder transportType={TransportTypeEnum.BUS} />}
          />
          <CustomRoute
            path={`${routes.TRIPS_CREATE_COOPERATIVE}`}
            bc={EmployeeAppLinksTitles.cooperative}
            render={() => <TransportOrder transportType={TransportTypeEnum.TAXI} />}
          />

          <CustomRoute
            path="*"
            component={(): JSX.Element => <Redirect to={!IS_REMOTE ? routes.TRIPS_CREATE_TAXI : '/'} />}
          />

        </CustomSwitch>
      </CustomRoute>
    </Switch>
  );
};

export default TripsRouter;
