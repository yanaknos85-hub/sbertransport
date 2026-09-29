import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { useSearchTransport } from 'api/transport/transport.api';
import { TransportSearchRequest, TransportStatus } from 'api/transport/transport.types';

import { List, Props as ListProps } from 'components/List/List';
import { RowIconTypes } from 'components/List/types';
import { Props as RowProps } from 'components/List/components/Row/Row';
import { defaultPagination } from 'hooks/useQuery';
import { useRoleMap } from 'hooks/useRoleMap';
import { useActiveTransport } from 'modules/Vehicles/context/ActiveTransport';

import { transportFormJSX } from './formJSX';
import { useModalForm } from '../../context/ModalForm';
import { processRequestTransportParams } from '../../utils';

export const TransportList: FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [query, setQuery] = useState<Record<string, any>>(defaultPagination);
  const { t } = useTranslation();
  const { handleOpenAddTransport } = useModalForm();

  const [useMainFilters, setUseMainFilters] = useState(true);

  const { contractorId, autoparkId } = useProfile().data;

  const { isAdmin, isManager } = useRoleMap();
  const { activeTransport, toggleActiveTransport } = useActiveTransport();

  const { content: transport, totalElements }
    = useSearchTransport(
      {
        page: defaultPagination,
        ...processRequestTransportParams(query as TransportSearchRequest),
        ...(isAdmin
          ? {
            contractorIds: useMainFilters ? [contractorId] : [query.contractorId],
            autoparkId: useMainFilters ? autoparkId : query.autoparkId,
          }
          : isManager
            ? {
              contractorIds: [contractorId],
              autoparkId: useMainFilters ? autoparkId : query.autoparkId,
            }
            : { contractorIds: [contractorId], autoparkId }
        ),
      }
    )?.data?.response || {};

  const rows: ListProps['rows']
    = transport?.map(item => ({
      rowData: item,
      activeRow: activeTransport,
      statuses: [
        { type: item.status === TransportStatus.IN_USE ? 'inUse' : 'notInUse' },
        { title: t.Vehicles.vinAbbr, description: item.vinCode },
      ] as RowProps['statuses'],
      iconType: RowIconTypes.SpecialCar,
      toggleActive: toggleActiveTransport as RowProps['toggleActive'],
    })) ?? [];

  const onChangeQuery = (values: Record<string, unknown>) => {
    if (useMainFilters && ('contractorId' in values || 'autoparkId' in values)) {
      setUseMainFilters(false);
    }
    setQuery(values);
  };

  return (
    <List
      autoSize
      onChangeQuery={onChangeQuery}
      queryParams={{ notUseUrl: ['contractorId', 'autoparkId'] }}
      onAddClick={handleOpenAddTransport}
      formJSX={transportFormJSX(t)}
      rows={rows}
      totalElements={totalElements}
      searchPanelProps={{
        searchValue: query.searchText,
        searchField: 'searchText',
        placeholder: 'Гос.номер или VIN',
        maxLength: 20,
      }}
    />
  );
};
