import * as routes from 'constants/constants.routes';
import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import Check from './images/Check';
import Diagram from './images/Diagram';

export const SubMenu = {
  isMain: [
    routes.TRIPS_CREATE_TAXI,
    routes.TRIPS_CREATE_PERSONAL,
    routes.TRIPS_CREATE_PUBLIC,
    routes.TRIPS_CREATE_CARSHARING,
    routes.TRIPS_CREATE_COOPERATIVE,
  ],
  approvements: [
    {
      to: `${routes.APPROVEMENT_TRIPS}/active`,
      name: EmployeeAppLinksTitles[EmployeeAppLinks.trips],
      icon: Check,
    },
  ],
  finances: [
    {
      to: `${routes.BONUSES_ACCOUNT_PAGE}`,
      name: `${EmployeeAppLinksTitles[EmployeeAppLinks.bonusesAccount]}`,
      icon: Diagram,
    },
  ],
};
