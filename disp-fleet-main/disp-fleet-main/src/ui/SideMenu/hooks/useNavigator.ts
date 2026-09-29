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
      case AppTitles.Vehicles:
        return location.pathname.includes(routes.VEHICLES);
      case AppTitles.Autoparks:
        return location.pathname.includes(routes.AUTOPARKS);
      case AppTitles.ReleaseOnLine:
        return location.pathname.includes(routes.RELEASE_ON_LINE_LINK);

      default:
        return false;
    }
  };

  const checkedActiveMainMenuItem = (name: string) => {
    switch (name) {
      case AppTitles.Autopark:
        return (
          location.pathname.includes(routes.VEHICLES)
          || location.pathname.includes(routes.AUTOPARKS)
          || location.pathname.includes(routes.RELEASE_ON_LINE_LINK)
        );

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
