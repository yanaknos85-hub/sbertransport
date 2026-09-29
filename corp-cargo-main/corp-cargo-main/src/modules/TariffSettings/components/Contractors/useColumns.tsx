import React, { useCallback, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { ColumnProps } from 'antd/lib/table';

import { useDeleteContractor } from 'api/contractors';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { ignore } from 'utils';
import { useModal } from './context/modal.context';

export type ContractorsRecord = Pick<
  Contractor,
  'name' | 'msrn' | 'tin' | 'id' | 'humanReadableId' | 'contactPersonInfo' | 'contactPersonPhone'
>;

export const useColumns: () => ColumnProps<ContractorsRecord>[] = () => {
  const { t } = useTranslation();
  const { openEdit } = useModal();

  const [deleteContractor] = useDeleteContractor();

  const onDelete = useCallback((id: string) => deleteContractor(id).catch(ignore), [deleteContractor]);

  return useMemo(
    () => [
      {
        title: t.contractors.contractorId,
        dataIndex: 'humanReadableId',
        width: 120,
        fixed: 'left',
      },
      {
        title: t.contractors.name,
        dataIndex: 'name',
        width: 220,
      },
      {
        title: t.contractors.tin,
        dataIndex: 'tin',
        width: 150,
      },
      {
        title: t.contractors.msrn,
        dataIndex: 'msrn',
        width: 160,
      },
      {
        title: t.contractors.contactPersonInfo,
        dataIndex: 'contactPersonInfo',
        width: 220,
      },
      {
        title: t.contractors.contactPersonPhone,
        dataIndex: 'contactPersonPhone',
        width: 160,
        render: phone => phone || '',
      },
      {
        title: t.contractors.contractorName,
        dataIndex: 'integrationParams',
        key: 'contractorName',
        width: 250,
        render: (integrationParams: Contractor['integrationParams']) => integrationParams?.contractorName,
      },
      {
        title: t.contractors.contractorRusName,
        dataIndex: 'integrationParams',
        key: 'contractorRusName',
        width: 210,
        render: (integrationParams: Contractor['integrationParams']) => integrationParams?.contractorRusName,
      },
      {
        title: t.contractors.integrationEmail,
        dataIndex: 'integrationParams',
        key: 'integrationEmail',
        render: (integrationParams: Contractor['integrationParams']) => integrationParams?.integrationEmail,
        width: 100,
      },
      {
        key: 'editDelete',
        render: (_, record) => (
          <TableEditButtons
            id={record.id}
            onEdit={openEdit}
            onDelete={onDelete}
          />
        ),
        fixed: 'right',
        width: 74,
      },
    ],
    [t, openEdit, onDelete]
  );
};
