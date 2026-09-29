interface AccessControlProps {
  userPermissions: string[];
  allowedPermissions: string[];
  children: JSX.Element;
  renderNoAccess?: () => JSX.Element | null;
}

export const checkPermissions = (userPermissions: string[], allowedPermissions: string[]) => {
  if (allowedPermissions.length === 0) {
    return true;
  }
  return userPermissions.some(permission => allowedPermissions.includes(permission));
};

const AccessControl = ({
  userPermissions,
  allowedPermissions,
  children,
  renderNoAccess,
}: AccessControlProps): JSX.Element | null => {
  const permitted = checkPermissions(userPermissions, allowedPermissions);
  return permitted ? children : renderNoAccess ? renderNoAccess() : null;
};

export default AccessControl;
