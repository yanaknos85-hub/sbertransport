import React, { useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { ColumnProps } from 'antd/lib/table';

import { useOrganizationProjection } from 'api/organizations/search';
import { useGetAvailableTransportTypes, useTransportServiceTypes } from 'api/transport-types';
import { useProfile } from 'api/profile';
import { useContractors } from 'api/contractors';
import {
  fullIntegrationTypeTitles, IntegrationTypes, sTransport,
  TariffTypes
} from 'constants/constants.app';
import { useDeleteTariff } from 'api/tariffs';
import { UUID } from 'utils/io-ts';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { ignore } from 'utils';
import { useModal } from '../context/modal.context';
import { TariffsJournal } from 'stores/Tariffs/Tariffs.interface';
import { StoreNames, useAppStore } from 'stores';

export const useColumns = () => {
  const { [StoreNames.tariffsStore]: tariffsStore } = useAppStore();
  const { tariffType } = useParams<{ tariffType: TariffTypes }>();

  const { openEdit } = useModal();

  const [deleteTariff] = useDeleteTariff();

  const { organizationId } = useProfile().data;

  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const transportServiceTypes = useTransportServiceTypes({ suspense: false }).data;
  const transportTypesOptions = useGetAvailableTransportTypes(organizationId, { suspense: false }).data;
  const contractorsOptions = useContractors({ config: { suspense: false } }).data?.contractors;

  const handleRefetchTariffs = useCallback(() => {
    tariffsStore.refetchSDOTariffs(tariffType);
  }, [tariffsStore, tariffType]);

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
        title: tariffType === TariffTypes.INCOME ? 'Агрегатор' : 'Исполнитель',
        key: 'contractorName',
        dataIndex: 'contractorName',
        width: 200,
        render: contractorName => tariffType === TariffTypes.INCOME ? sTransport : (contractorName ?? '-'),
      },
      {
        title: 'Номер договора',
        dataIndex: 'contractNumber',
        width: 200,
      },
      {
        title: tariffType === TariffTypes.INCOME ? 'Организация-клиент' : 'Агрегатор',
        dataIndex: 'organizationId',
        key: 'organizationId',
        width: 200,
        render: orgId => (
          tariffType === TariffTypes.INCOME
            ? organizations?.find(x => x.id === orgId)?.officialName ?? organizationId
            : sTransport
        ),
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
        render: (_, record) => record.active && (
          <TableEditButtons
            id={record.id}
            onEdit={id => openEdit(id, record.transportType)}
            onDelete={tariffId => deleteTariff({
              tariffId: tariffId as UUID,
              transTypeId: record.transportType.toLowerCase(),
              refetchTariffs: handleRefetchTariffs,
            }).catch(ignore)}
          />
        ),
        fixed: 'right',
        width: 74,
      },
    ],
    [
      tariffType,
      transportServiceTypes,
      transportTypesOptions,
      contractorsOptions,
      organizations,
      organizationId,
      openEdit,
      deleteTariff,
      handleRefetchTariffs,
    ]
  );

  return columns;
};
