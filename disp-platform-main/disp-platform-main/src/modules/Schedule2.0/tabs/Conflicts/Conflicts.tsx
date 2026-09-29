import React, { FC } from 'react';

import { Pagination } from '@sber-sbertransport/ui-kit/src';

import { Table } from 'antd';

import { useShiftConflicts } from 'api/shift-conflicts/shift-conflicts.api';
import { ShiftConflictsFilters } from 'api/shift-conflicts/shift-conflicts.types';

import { useQuery } from 'hooks/useQuery';

import { useTranslation } from 'i18n';

import { SearchPanel } from 'components/SearchPanel/SearchPanel';

import { useConflictsColumns } from './hooks/useConflictsColumns';
import { DeleteConflictModal } from './modals/DeleteConflictModal';

import styles from './Conflicts.module.scss';

const Conflicts: FC = () => {
  const { t } = useTranslation();
  const columns = useConflictsColumns();

  const {
    query, setQuery, setPagination,
  } = useQuery<ShiftConflictsFilters>();

  const { data, isLoading } = useShiftConflicts(query, { suspense: false });

  const search = (value: string) => {
    if (!value || value.length >= 3) {
      setQuery({
        stateNumber: value || undefined,
      });
    }
  };

  return (
    <>
      <div className={styles.filters}>
        <SearchPanel
          value={query.stateNumber}
          placeholder={t.Shifts.searchStateNumber}
          onSearch={search}
          className={styles.search}
        />
      </div>

      <Table
        dataSource={data?.content}
        columns={columns}
        rowKey="routeId"
        loading={isLoading}
        className={styles.table}
        scroll={{ x: 'max-content' }}
        pagination={false}
      />

      <Pagination
        pagination={query}
        total={data?.totalElements ?? 0}
        setPagination={setPagination}
        className={styles.pagination}
      />

      <DeleteConflictModal />
    </>
  );
};

export default Conflicts;
