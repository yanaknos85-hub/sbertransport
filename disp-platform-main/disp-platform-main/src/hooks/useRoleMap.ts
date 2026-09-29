import { useMemo } from 'react';
import { useRole } from 'hooks/useRole';
import { Roles } from 'constants/app.constants';

export const useRoleMap = () => {
  const roles = useRole();

  return useMemo(() => ({
    /** Администратор автопарков */
    isAdmin:
      roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
      || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR),

    /** Менеджер автопарка */
    isManager: roles.includes(Roles.ROLE_MAIN_DISPATCHER_CONTRACTOR),

    /** Диспетчер филиала автопарка */
    isDispatcher: roles.includes(Roles.ROLE_DISPATCHER_CONTRACTOR),
  }), [roles]);
};
