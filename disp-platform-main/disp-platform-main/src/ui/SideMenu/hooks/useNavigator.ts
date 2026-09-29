import { useLocation } from 'react-router-dom';

import { AppTitles } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

const useNavigator = (): {
  checkedActiveMenuItem(name: string): boolean;
  checkedActiveMainMenuItem(name: string): boolean;
} => {
  const location = useLocation();

  const checkedActiveMenuItem = (name: string) => {
    switch (name) {
      case AppTitles.Disp:
        return location.pathname.includes(routes.TRIPS);
      case AppTitles.Crews:
        return location.pathname.includes(routes.SCHEDULE);
      case AppTitles.Staff:
        return location.pathname.includes(routes.STAFF);
      case AppTitles.Reports:
        return location.pathname.includes(routes.REPORTS);
      case AppTitles.Support:
        return location.pathname.includes(routes.SUPPORT);

      default:
        return false;
    }
  };

  const checkedActiveMainMenuItem = (name: string) => {
    switch (name) {
      default:
        return false;
    }
  };

  return {
    checkedActiveMenuItem,
    checkedActiveMainMenuItem,
  };
};

export default useNavigator;
