import React, { useMemo } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { useOrganizationProjection } from 'api/organizations/search';
import { useGetAvailableTransportTypes, useTransportServiceTypesCargo } from 'api/transport-types';
import { useProfile } from 'api/profile';
import { useContractors } from 'api/contractors';
import { fullIntegrationTypeTitles, IntegrationTypes } from 'constants/constants.app';
import { useDeleteTariff } from 'api/tariffs-cargo';
import { UUID } from 'utils/io-ts';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { ignore } from 'utils';
import { useModal } from '../context/modal.context';
import { TariffsJournal } from 'stores/Tariffs/Tariffs.interface';

// вместо any указать тип
export const useColumns = () => {
  const { openEdit } = useModal();

  const [deleteTariff] = useDeleteTariff();

  const { organizationId } = useProfile().data;

  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const transportServiceTypes = useTransportServiceTypesCargo({ suspense: false }).data;
  const transportTypesOptions = useGetAvailableTransportTypes(organizationId, { suspense: false }).data;
  const contractorsOptions = useContractors({ config: { suspense: false } }).data?.contractors;

  const columns = useMemo<ColumnProps<TariffsJournal>[]>(
    () => [
      {
        title: 'Тариф',
        dataIndex: 'humanReadableId',
        fixed: 'left',
        width: 180,
      },
      {
        title: 'Статус',
        dataIndex: 'active',
        width: 100,
        render: active => (active ? 'Активен' : 'Отключен'),
      },
      {
        title: 'Услуга',
        dataIndex: 'serviceType',
        width: 200,
        render: serviceType => transportServiceTypes?.find(x => x.name === serviceType)?.rusName ?? serviceType,
      },
      {
        title: 'Вид транспорта',
        dataIndex: 'transportType',
        width: 200,
        render: transportType => transportTypesOptions?.find(x => x.name === transportType)?.rusName ?? transportType,
      },
      {
        title: 'Контрагент',
        dataIndex: 'contractorId',
        key: 'contractorId',
        width: 200,
        render: contractorId => contractorsOptions?.find(x => x.id === contractorId)?.name ?? '-',
      },
      {
        title: 'Номер контракта',
        dataIndex: 'contractNumber',
        width: 200,
      },
      {
        title: 'Подразделение',
        dataIndex: 'departmentHumanReadableId',
        key: 'departmentHumanReadableId',
        width: 200,
      },
      {
        title: 'Территория действия тарифа',
        dataIndex: 'region',
        key: 'region',
        width: 200,
      },
      {
        title: 'Тип интеграции',
        dataIndex: 'contractorId',
        key: 'integrationType',
        width: 200,
        render: contractorId => fullIntegrationTypeTitles[
          contractorsOptions?.find(x => x.id === contractorId)?.integrationType as IntegrationTypes
        ] ?? '-',
      },
      {
        key: 'editDelete',
        render: (_, record) => (
          <TableEditButtons
            id={record.id}
            onEdit={id => openEdit(id, record.transportType)}
            onDelete={tariffId => deleteTariff({
              tariffId: tariffId as UUID,
              transTypeId: record.transportType.toLowerCase(),
            }).catch(ignore)}
          />
        ),
        fixed: 'right',
        width: 74,
      },
    ],
    [
      transportServiceTypes,
      transportTypesOptions,
      contractorsOptions,
      organizations,
      organizationId,
      openEdit,
      deleteTariff,
    ]
  );

  return columns;
};
