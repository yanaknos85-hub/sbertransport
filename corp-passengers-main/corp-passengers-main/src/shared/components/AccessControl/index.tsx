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
}

export const checkPermissions = ({
  userPermissions,
  allowedPermissions,
  allowedUserType,
  orgStructureType,
}: {
  userPermissions: string[];
  allowedPermissions: string[];
  allowedUserType?: OrgStructureType;
  orgStructureType?: OrgStructureType;
}) => {
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
  return userPermissions.some(permission => allowedPermissions.includes(permission));
};

const AccessControl = ({
  userPermissions,
  allowedPermissions,
  allowedUserType,
  children,
  renderNoAccess,
}: AccessControlProps): JSX.Element | null => {
  const { orgStructureType } = useProfile().data;
  const roles = useRole();

  const permitted = checkPermissions({
    userPermissions: userPermissions ?? roles,
    allowedPermissions,
    allowedUserType,
    orgStructureType,
  });

  return permitted ? children : renderNoAccess ? renderNoAccess() : null;
};

export default AccessControl;
