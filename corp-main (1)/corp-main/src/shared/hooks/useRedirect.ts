import { useMemo, useEffect } from 'react';
import { useHistory } from 'react-router-dom';

import mfDataLoader from 'mf/MFDataLoader';
import { Roles, FleetSingleRoles } from 'constants/constants.app';
import { useRole } from 'utils/useRole';

const routesBoot = mfDataLoader(() => {
  try {
    return import('fleet/routes');
  } catch {
    return Promise.resolve();
  }
});

// Переадресация во fleet сервис для некоторых ролей при наличии единственной роли
const useRedirect = () => {
  const history = useHistory();
  const roles = useRole();
  const hasSingleFleetRole = roles.length === 1 && FleetSingleRoles.includes(roles[0] as Roles);
  const routes: Record<string, string> = useMemo(() => routesBoot.data, []);

  useEffect(() => {
    if (!!routes.FLEET_RELEASE && hasSingleFleetRole)
      history.replace(routes.FLEET_RELEASE);
  }, [routes]);
};

export default useRedirect;
