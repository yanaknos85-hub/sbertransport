import { useLocation, useRouteMatch } from 'react-router-dom';

import { EmployeeAppLinksTitles } from 'constants/constants.app';

import { useMemo } from 'react';

import { MFBootRoutes, MFRoutes } from 'ui/SideMenu/SideMenu';

const useNavigator = (): {
  isCreateSubPage(): boolean;
  isTripsSubPage(): boolean;
  isApprovementsSubPage(): boolean;
  isLimitsSubPage(): boolean;
  isApplicationsSubPage(): boolean;
  isCargosSubPage(): boolean;
  isFavoriteSubPage(): boolean;
  isCreateSupportPage(): boolean;
  checkedActiveMenuItem(name: string): boolean;
  checkedActiveMainMenuItem(name: string): boolean;
} => {
  const location = useLocation();
  const match = useRouteMatch();

  const MFRoutes = useMemo(() => MFBootRoutes, []) as Record<string, MFRoutes>;

  const isCreateSubPage = (): boolean => [
    `client/create`,
    `client/create/taxi`,
    `client/create/personal`,
    `client/create/cargo`,
    `client/create/public`,
    `client/create/carsharing`,
    `client/create/cooperative`,
  ].includes(location.pathname);

  const isCreateSupportPage = (): boolean => [
    `/client/support`,
  ].includes(location.pathname);

  const isTripsSubPage = (): boolean => [
    `client/trip/planned`,
    `client/trip/final`,
  ].includes(location.pathname);

  const isCargosSubPage = (): boolean => [
    `client/cargos/active`,
    `client/cargos/final`,
  ].includes(location.pathname);

  const isApprovementsSubPage = (): boolean => [
    `${MFRoutes.cargo.data.APPROVEMENT_REGULAR_CARGOS}/active`,
    `${MFRoutes.cargo.data.APPROVEMENT_CARGOS}/active`,
    `${MFRoutes.passengers.data.APPROVEMENT_TRIPS}/active`,
    `client/approvement/requests/active`,
    `client/approvement/limits/active`,
    `client/approvement/delegates`,
    `client/approvement/cargos/active`,
  ].includes(location.pathname);

  const isLimitsSubPage = (): boolean => [
    `client/limits/limitsInfo`,
    `client/bonuses/account`,
    `client/limits/limitRequests/active`,
    `client/limits/limitRequests/closed`,
  ].includes(location.pathname);

  const isApplicationsSubPage = (): boolean => [
    `client/passengers/trip/list/planned`,
    `client/passengers/trip/list/planned/:id`,
    `client/passengers/trip/list/final`,
    `client/cargo/single/list/active`,
    `client/cargo/regular/list/active`,
    `client/fleet/my-orders/repair`,
  ].includes(location.pathname);

  const isFavoriteSubPage = (): boolean => [`client/favorite`].includes(location.pathname);

  const checkedActiveMenuItem = (name: string) => {
    switch (name) {
      case EmployeeAppLinksTitles['trips']:
        return location.pathname.includes(`${match.path}/passengers/trips/list/`);
      case EmployeeAppLinksTitles['yandexTrips']:
        return location.pathname.includes(`${match.path}/passengers/trips/yandex`);
      case EmployeeAppLinksTitles['cargos']:
        return location.pathname.includes(`${match.path}/cargo/single/list`);
      case EmployeeAppLinksTitles['carService']:
        return location.pathname.includes(`${match.path}/fleet/my-orders`);
      case `Согласование ${EmployeeAppLinksTitles['trips']}`:
        return location.pathname.includes(`${MFRoutes.passengers.data.APPROVEMENT_TRIPS}`);
      case `Согласование ${EmployeeAppLinksTitles['cargos']}`:
        return location.pathname.includes(`${MFRoutes.cargo.data.APPROVEMENT_CARGOS}`);
      case `Согласование ${EmployeeAppLinksTitles['regularCargos']}`:
        return location.pathname.includes(`${MFRoutes.cargo.data.APPROVEMENT_REGULAR_CARGOS}`);
      case `Согласование ${EmployeeAppLinksTitles['limitsInfo']}`:
        return location.pathname.includes(`${match.path}/approvement/limits`);
      case `Согласование ${EmployeeAppLinksTitles['delegates']}`:
        return location.pathname.includes(`${match.path}/approvement/delegates`);
      case `Финансы ${EmployeeAppLinksTitles['limitsInfo']}`:
        return location.pathname.includes(`${match.path}/limits/limitsInfo`);
      case `Финансы ${EmployeeAppLinksTitles['bonusesAccount']}`:
        return location.pathname.includes(`${match.path}/bonuses`);
      case `Финансы ${EmployeeAppLinksTitles['limits']}`:
        return location.pathname.includes(`${match.path}/limits/limitRequests`);

      default:
        return false;
    }
  };

  const checkedActiveMainMenuItem = (name: string) => {
    switch (name) {
      case 'Главная':
        return location.pathname.includes(`client/home`)
          || location.pathname.includes(`client/create`)
          || location.pathname.includes(`client/profile`)
          || location.pathname.includes(`${match.path}/cargo/single/create`)
          || location.pathname.includes(`${match.path}/fleet/parking`)
          || location.pathname.includes(`${match.path}/fleet/order`)
          || location.pathname.includes(`client/passengers/trips/create`)
          || location.pathname.includes(`client/passengers/vehicles`);
      case 'Мои заявки':
        return location.pathname.includes(`client/passengers/trips/list`)
          || location.pathname.includes(`${match.path}/cargo/single/list`)
          || location.pathname.includes(`${match.path}/fleet/my-orders`);
      case 'Согласования':
        return location.pathname.includes(`${MFRoutes.passengers.data.APPROVEMENT_TRIPS}`)
          || location.pathname.includes(`${MFRoutes.cargo.data.APPROVEMENT_CARGOS}`)
          || location.pathname.includes(`${MFRoutes.cargo.data.APPROVEMENT_REGULAR_CARGOS}`)
          || location.pathname.includes(`${match.path}/approvement/limits`)
          || location.pathname.includes(`${match.path}/approvement/delegates`);
      case 'Финансы':
        return location.pathname.includes(`${match.path}/limits/limitsInfo`)
          || location.pathname.includes(`${match.path}/bonuses`)
          || location.pathname.includes(`${match.path}/limits/limitRequests`);

      default:
        return false;
    }
  };

  return {
    isCreateSubPage,
    isTripsSubPage,
    isApprovementsSubPage,
    isLimitsSubPage,
    isApplicationsSubPage,
    isCargosSubPage,
    isFavoriteSubPage,
    isCreateSupportPage,
    checkedActiveMenuItem,
    checkedActiveMainMenuItem,
  };
};

export default useNavigator;
