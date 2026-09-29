import * as routes from 'constants/constants.routes';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from '../../constants/EmployeeApp.constants';
import Check from './images/Check';

export const SubMenu = {
  isMain: [routes.CARGO_CREATE, routes.CARGOS, routes.REGULAR_CARGO_CREATE, routes.REGULAR_CARGOS, routes.MULTIPLE_CARGO_CREATE],
  approvements: [
    {
      to: `${routes.APPROVEMENT_CARGOS}/active`,
      name: EmployeeAppLinksTitles[EmployeeAppLinks.cargos],
      icon: Check,
    },
    {
      to: `${routes.APPROVEMENT_REGULAR_CARGOS}/active`,
      name: EmployeeAppLinksTitles[EmployeeAppLinks.regularCargos],
      icon: Check,
    },
  ],
};
