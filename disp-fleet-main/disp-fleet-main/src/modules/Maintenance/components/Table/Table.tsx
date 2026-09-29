import React, { FC } from 'react';

import { useMaintenanceMonitor } from 'api/maintenance/maintenance.api';
import { MaintenanceFilters } from 'api/maintenance/maintenance.types';
import { useProfile } from 'api/profile/profile.api';

import { PaginationParams } from 'utils/io-ts/pagination';

import { TableStyled } from 'components/TableStyled';

import { useColumns } from './useColumns';

interface Props {
  query: MaintenanceFilters;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<Props> = ({ query, setPagination }) => {
  const { contractorId, autoparkId } = useProfile().data;

  const { columns } = useColumns(query.type);

  const { data } = useMaintenanceMonitor({
    ...query,
    contractorIdSet: contractorId ? [contractorId] : [],
    autoparkIdSet: autoparkId ? [autoparkId] : [],
  });

  return (
    <TableStyled
      dataSource={data?.content}
      columns={columns}
      paginationParams={query}
      setPagination={setPagination}
      total={data?.totalElements ?? 0}
      autoHeight
    />
  );
};

export default Table;
