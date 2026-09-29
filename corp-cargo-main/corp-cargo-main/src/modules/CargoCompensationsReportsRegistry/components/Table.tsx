import React, { FC, Suspense } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { Table } from 'antd';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { useTasks } from '../hooks/useTasks';
import { useColumns } from '../hooks/useColumns';
import { CompensationReportsJournal } from '../types';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}
interface TasksSearchQuery {
  pagination: PaginationParams;
}

const TasksTable: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { content, totalElements } = useTasks(query).data;
  const columns = useColumns();
  return (
    <Table
      columns={columns}
      dataSource={content as unknown as CompensationReportsJournal[]}
      scroll={{ x: '100%' }}
      rowKey="id"
      pagination={{
        current: query.page + 1,
        pageSize: query.size,
        total: totalElements,
        onChange: (page, pageSize) => {
          setPagination({
            page: page - 1,
            size: pageSize,
          });
        },
      }}
    />
  );
};

const TasksTab = () => {
  const {
    query, setPagination,
  } = useQuery<TasksSearchQuery>();

  return (
    <div>
      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <TasksTable
            query={query}
            setPagination={setPagination}
          />
        </Suspense>
      </ErrorBoundary>
    </div>
  );
};

export default TasksTab;
