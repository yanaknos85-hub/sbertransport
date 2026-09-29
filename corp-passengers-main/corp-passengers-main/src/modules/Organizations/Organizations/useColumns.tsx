import React, { useMemo } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';

import { Organization } from 'stores/Organizations/Organizations.interface';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { useDeleteOrganization } from 'api/organizations';
import { useModal } from './context/modal.context';

export type OrganizationRecord = Pick<Organization, 'id' | 'officialName' | 'address' | 'contacts' | 'msrn' | 'tid'>;

export const useColumns: () => ColumnProps<OrganizationRecord>[] = () => {
  const { t } = useTranslation();
  const { openEdit } = useModal();
  const history = useHistory();
  const { url } = useRouteMatch();

  const onEdit = (id: string) => {
    history.push(`${url}/${id}`);
  };

  const [deleteOrganization] = useDeleteOrganization();

  return useMemo(() => [
    {
      title: t.Forms.organizationDetailedForm.id,
      dataIndex: 'id',
      key: 'id',
      width: 280,
    },
    {
      title: t.Forms.organizationDetailedForm.organizationalUnitCode,
      dataIndex: 'organizationCode',
      key: 'organizationCode',
      width: 280,
    },
    {
      title: t.Forms.organizationDetailedForm.officialName,
      dataIndex: 'officialName',
      key: 'officialName',
      width: 280,
    },
    {
      title: t.Forms.organizationDetailedForm.address,
      dataIndex: 'address',
      key: 'address',
    },
    {
      title: t.Forms.organizationDetailedForm.organizationGroup,
      dataIndex: 'organizationGroup',
      key: 'organizationGroup',
    },
    {
      title: t.Contacts.PHONE,
      dataIndex: 'contacts',
      key: 'contacts',
      render: contacts => contacts?.filter((contact: Record<string, string>) => contact.type === 'PHONE')[0]?.value,
    },
    {
      title: t.Contacts.EMAIL,
      dataIndex: 'contacts',
      key: 'contacts',
      render: contacts => contacts?.filter((contact: Record<string, string>) => contact.type === 'EMAIL')[0]?.value,
    },
    {
      title: t.Contacts.SITE,
      dataIndex: 'contacts',
      key: 'contacts',
      render: contacts => contacts?.filter((contact: Record<string, string>) => contact.type === 'SITE')[0]?.value,
    },
    {
      title: t.Forms.organizationDetailedForm.msrn,
      dataIndex: 'msrn',
      key: 'msrn',
    },
    {
      title: t.Forms.organizationDetailedForm.tid,
      dataIndex: 'tid',
      key: 'tid',
    },
    {
      title: t.Forms.organizationDetailedForm.easupId,
      dataIndex: 'easupId',
      key: 'easupId',
      width: 280,
    },
    // DELETE не отрабатывал и до перенесения кнопки
    {
      key: 'editDelete',
      render: (_, { id }) => (
        <TableEditButtons
          id={id}
          onEdit={() => onEdit(id)}
          onDelete={deleteOrganization}
        />
      ),
      fixed: 'right',
      width: 74,
    },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [t, openEdit, deleteOrganization, onEdit]);
};
