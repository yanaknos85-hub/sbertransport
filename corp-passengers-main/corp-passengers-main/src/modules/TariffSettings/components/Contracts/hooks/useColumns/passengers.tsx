/* eslint-disable react-hooks/rules-of-hooks */
import React, { useMemo } from 'react';
import moment from 'moment';
import type { ColumnProps } from 'antd/lib/table';

import { useDeleteContract } from 'api/contracts';
import { useServiceTypes } from 'api/service-types';
import { useTransportTypes } from 'api/transport-types';
import { DATE_FORMAT } from 'constants/constants.app';
import { useTranslation } from 'i18n';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import type { Contract } from 'stores/Contracts/Contracts.interface';
import type { UUID } from 'utils/io-ts';
import { formatRubles } from 'utils';
import { useModal } from '../../context/modal.context';

export const passengers = () => {
  const { t } = useTranslation();
  const { openEdit } = useModal();
  const serviceTypes = useServiceTypes({ suspense: false }).data;
  const transportTypes = useTransportTypes({ suspense: false }).data;
  const [deleteContract] = useDeleteContract();

  return useMemo<ColumnProps<Contract>[]>(
    () => [
      {
        dataIndex: 'active',
        title: t.Contracts.active,
        render: value => t.Contracts[value ? 'activated' : 'deactivated'],
        width: 100,
      },
      {
        dataIndex: 'startDate',
        title: t.Contracts.startDate,
        render: value => moment(value).format(DATE_FORMAT.BASE_REVERTED),
        width: 110,
      },
      {
        dataIndex: 'endDate',
        title: t.Contracts.endDate,
        render: value => (value ? moment(value).format(DATE_FORMAT.BASE_REVERTED) : t.Contracts.endless),
        width: 110,
      },
      {
        dataIndex: 'serviceType',
        title: t.Contracts.serviceType,
        width: 200,
        render: () => serviceTypes?.[0]?.name,
      },
      {
        dataIndex: 'transportType',
        title: t.Contracts.transportType,
        width: 220,
        render: transportType => transportTypes?.find(t => t.name === transportType)?.rusName ?? transportType,
      },
      {
        dataIndex: 'contractorName',
        title: t.Contracts.contractorId,
        width: 250,
      },
      {
        dataIndex: 'contractNumber',
        title: t.Contracts.number,
        width: 160,
      },
      {
        dataIndex: 'uvhd',
        title: t.Contracts.uvhd,
        width: 160,
      },
      {
        dataIndex: 'sum',
        title: t.Contracts.sum,
        align: 'right',
        render: (value: number) => formatRubles(value),
        width: 200,
      },
      {
        dataIndex: 'region',
        title: t.Contracts.region,
        width: 250,
      },
      {
        key: 'editDelete',
        render: (_, record) => (
          <TableEditButtons
            id={record.id}
            onEdit={id => openEdit(id)}
            onDelete={contractId => deleteContract({ contractId: contractId as UUID })}
          />
        ),
        fixed: 'right',
        width: 74,
      },
    ],
    [t.Contracts, serviceTypes, transportTypes, openEdit, deleteContract]
  );
};
