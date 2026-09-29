import { useRole } from 'utils/useRole';

// Данный хук работает только в рамках этого компонента, ВЛП-не имеет роли(ROLE_OWNER_LIMIT_DEPARTMENT) и поэтому нельзя использовать useRole
// Если юзер - ВЛП, то даем доступ к перераспределению лимитов на равне с админом смд и админом корп клиента
export function useRoleAccessControl(department: any, userId: string): string[] {
  const roles = useRole();

  if (department?.departmentHead?.id === userId) {
    return [...roles, 'ROLE_OWNER_LIMIT_DEPARTMENT'];
  }
  return roles;
}
