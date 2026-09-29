import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { ColumnProps } from 'antd/lib/table';
import { useOrganizationProjection } from 'api/organizations/search';
import { useDeleteExecutorGroup } from 'api/executor-group';

import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { useModal } from './context/modal.context';

export type ContractorsRecord = Pick<
  Contractor,
  'name' | 'msrn' | 'tin' | 'id' | 'humanReadableId' | 'contactPersonInfo' | 'contactPersonPhone'
>;

export const useColumns: () => ColumnProps<ContractorsRecord>[] = () => {
  const { t } = useTranslation();
  const { openEdit } = useModal();
  const [deleteExecutorGroup] = useDeleteExecutorGroup();

  const organizationsList = useOrganizationProjection({}, { suspense: false }).data;

  return useMemo(
    () => [
      {
        title: 'Наименование группы исполнителя',
        dataIndex: 'name',
        width: 350,
      },
      {
        title: 'Организация заказчика',
        dataIndex: 'organizationId',
        width: 300,
        render: organizationId => organizationsList ? organizationsList.find(item => item.id === organizationId)?.officialName : '',
      },
      {
        title: 'Территория заказчика',
        dataIndex: 'geoZones',
        width: 150,
        render: geoZones => geoZones[0]?.name || '',
      },
      {
        title: 'Организация исполнителей',
        dataIndex: 'organizations',
        width: 300,
        render: organizations => organizations.reduce((acc, item, i, arr) => acc + item.officialName + (arr.length > i + 1 ? ', ' : ''), '') || '-',
      },
      {
        title: 'Подразделение',
        dataIndex: 'departments',
        width: 220,
        render: departments => departments.reduce((acc, item, i, arr) => acc + item.humanReadableId + (arr.length > i + 1 ? ', ' : ''), '') || '-',
      },
      {
        title: 'ФИО заказчика',
        dataIndex: 'customers',
        width: 220,
        render: customers => customers[0]?.name || '',
      },
      {
        title: 'ФИО исполнителей',
        dataIndex: 'executors',
        width: 250,
        render: executors => executors.reduce((acc, item, i, arr) => acc + item.employeeName + ` (${item.employeePersonnelNumber})` + (arr.length > i + 1 ? ', ' : ''), '') || '-',
      },
      {
        title: 'Активность группы',
        dataIndex: 'active',
        width: 150,
        render: active => active ? 'активная' : 'неактивная',
      },
      {
        key: 'editDelete',
        render: (_, record) => (
          <TableEditButtons
            id={record.id}
            onEdit={() => openEdit(record.id)}
            onDelete={() => deleteExecutorGroup({ executorGroupId: record.id })}
            title="Удалить группу исполнителей?"
          />
        ),
        fixed: 'right',
        width: 74,
      },
    ],
    [t, organizationsList?.length, openEdit]
  );
};
