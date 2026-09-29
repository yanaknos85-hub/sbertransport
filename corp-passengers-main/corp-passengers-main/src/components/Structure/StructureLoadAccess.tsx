import React, { ReactNode, useMemo } from 'react';

import { useProfile } from 'api/profile';
import { useGetOrganizationById } from 'api/organizations';
import { ToolbarElement } from 'components/Toolbar';
import AccessControl from 'shared/components/AccessControl';
import { Roles } from 'constants/constants.app';
import { b64EncodeUnicode } from 'utils/Misc';

export const StructureLoadAccess = ({ children }: { children: ReactNode }) => {
  const { organizationId } = useProfile().data;
  const organization = useGetOrganizationById(organizationId).data;

  if (organization.easupId) return null;

  return (
    <AccessControl
      allowedPermissions={[Roles.ADMIN_CORP_CLIENT, Roles.MAINTENANCE_ENGINEER, Roles.ENGINEER_CORP_CLIENT]}
      renderNoAccess={() => null}
    >
      <ToolbarElement>{children}</ToolbarElement>
    </AccessControl>
  );
};

export const useDownloadParams = () => {
  const { organizationId } = useProfile().data;
  const organization = useGetOrganizationById(organizationId).data;

  return useMemo(
    () => ({
      filters: b64EncodeUnicode(JSON.stringify({ organizationId: organization.id })),
    }),
    [organization]
  );
};
