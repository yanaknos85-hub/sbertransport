import { useProfile } from 'api/profile';
import { OrgStructureType } from 'constants/constants.app';
import { isTestMode } from 'utils/isTestMode';
import { isTestStand } from 'utils/isTestStand';
import { useRole } from 'utils/useRole';

interface AccessControlProps {
  userPermissions?: string[];
  allowedPermissions: string[];
  children: JSX.Element;
  renderNoAccess?: () => JSX.Element | null;
  allowedUserType?: OrgStructureType;
  isBundleRoles?: boolean;
}

type PermissionsProps = Omit<AccessControlProps, 'children' | 'renderNoAccess'> & {
  userPermissions: string[];
  orgStructureType?: OrgStructureType;
};

export const checkPermissions = ({
  userPermissions,
  allowedPermissions,
  allowedUserType,
  orgStructureType,
  isBundleRoles,
}: PermissionsProps) => {
  // В тестовом режиме на тестовых стендах разрешаем доступ всем и везде
  if (isTestMode() && isTestStand()) {
    return true;
  }

  if (allowedUserType && allowedUserType !== orgStructureType) {
    return false;
  }

  if (allowedPermissions.length === 0) {
    return true;
  }

  if (isBundleRoles) {
    return allowedPermissions.every(permission => userPermissions.includes(permission));
  }

  return userPermissions.some(permission => allowedPermissions.includes(permission));
};

const AccessControl = ({
  userPermissions,
  allowedPermissions,
  allowedUserType,
  children,
  renderNoAccess,
  isBundleRoles,
}: AccessControlProps): JSX.Element | null => {
  const { orgStructureType } = useProfile().data;
  const roles = useRole();

  const permitted = checkPermissions({
    userPermissions: userPermissions ?? roles,
    allowedPermissions,
    allowedUserType,
    orgStructureType,
    isBundleRoles,
  });

  return permitted ? children : renderNoAccess ? renderNoAccess() : null;
};

export default AccessControl;
