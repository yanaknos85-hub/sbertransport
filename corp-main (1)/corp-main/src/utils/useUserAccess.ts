import { useMemo } from 'react';
import { AcceptableUserRoles } from 'constants/constants.app';
import { jwtDecode } from './Misc';
import { useToken } from './useToken';

export const useVerifyUserAccess = () => {
  const token = useToken();

  return useMemo(() => {
    if (!token) return false;

    const { roles } = jwtDecode<{ scope: string; roles: string[] }>(token);

    return roles.some(role => AcceptableUserRoles.includes(role));
  }, [token]);
};
