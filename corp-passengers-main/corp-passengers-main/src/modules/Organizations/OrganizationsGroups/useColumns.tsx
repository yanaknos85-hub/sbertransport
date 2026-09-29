import React, { useMemo } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import { useDeleteOrganizationsGroup } from 'api/organizations/organizations-groups';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { OrganizationsGroup } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';

export type OrganizationsGroupsRecord = Pick<OrganizationsGroup, 'id' | 'name'>;

export const useColumns: (
  setGroupEdit: (id: OrganizationsGroup | undefined) => void,
) => ColumnProps<OrganizationsGroupsRecord>[] = setGroupEdit => {
  const { t } = useTranslation();

  const [deleteOrganizationsGroup] = useDeleteOrganizationsGroup();

  return useMemo(
    () => [
      {
        title: t.Forms.organizationsGroupsDetailedForm.id,
        dataIndex: 'id',
        width: 350,
      },
      {
        title: t.Forms.organizationsGroupsDetailedForm.name,
        dataIndex: 'name',
      },
      {
        key: 'editDelete',
        render: (_, group) => (
          <TableEditButtons
            id={group.id}
            onEdit={() => setGroupEdit(group)}
            onDelete={() => deleteOrganizationsGroup(group.id)}
          />
        ),
        fixed: 'right',
        width: 5,
      },
    ],
    [t, deleteOrganizationsGroup, setGroupEdit]
  );
};
