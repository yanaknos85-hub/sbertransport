interface AccessControlProps {
  userPermissions: string[];
  allowedPermissions: string[];
  children: JSX.Element;
  renderNoAccess?: () => JSX.Element | null;
}

export const checkPermissions = (userPermissions, allowedPermissions) => (
  !allowedPermissions.length || userPermissions.some(p => allowedPermissions.includes(p))
);

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
