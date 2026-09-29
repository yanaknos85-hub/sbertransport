import { useLocation } from 'react-router-dom';

import { CargosTabsFilters } from 'constants/Cargo.constants';
import { AppLinksStartPage } from 'constants/constants.app';
import { EmployeeAppLinks } from 'constants/EmployeeApp.constants';
import { Helpers as FleetManagementHelpers } from 'modules/FleetManagement';

const useNavigator = (): {
  isCreateSubPage(): boolean;
  isTripsSubPage(): boolean;
  isApprovementsSubPage(): boolean;
  isLimitsSubPage(): boolean;
  isCargosSubPage(): boolean;
  isRegularCargosSubPage(): boolean;
  isFavoriteSubPage(): boolean;
  isParkingsSubPage(): boolean;
} => {
  const location = useLocation();

  const isCreateSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.taxi}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.personal}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.cargo}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.public}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.carsharing}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.create}/${EmployeeAppLinks.cooperative}`,
  ].includes(location.pathname);

  const isTripsSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.trips}/planned`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.trips}/final`,
  ].includes(location.pathname);

  const isCargosSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.cargos}/active`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.cargos}/final`,
  ].includes(location.pathname);

  const isRegularCargosSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.regularCargos}/active`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.regularCargos}/final`,
  ].includes(location.pathname);

  const isApprovementsSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/requests/active`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.limits}/active`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.delegates}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.cargos}/${CargosTabsFilters.active}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.compensation}/active`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.compensation}/final`,
  ].includes(location.pathname);

  const isLimitsSubPage = (): boolean => [
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.limits}/${EmployeeAppLinks.limitsInfo}`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.bonuses}/account`,
    `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.limits}/${EmployeeAppLinks.limitRequests}/active`,
  ].includes(location.pathname);

  const isFavoriteSubPage = (): boolean => [`${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.favorite}`].includes(location.pathname);

  const isParkingsSubPage = (): boolean => FleetManagementHelpers.getFullLinks(AppLinksStartPage.EmployeesApp).includes(location.pathname);

  return {
    isCreateSubPage,
    isTripsSubPage,
    isApprovementsSubPage,
    isLimitsSubPage,
    isCargosSubPage,
    isRegularCargosSubPage,
    isFavoriteSubPage,
    isParkingsSubPage,
  };
};

export default useNavigator;
