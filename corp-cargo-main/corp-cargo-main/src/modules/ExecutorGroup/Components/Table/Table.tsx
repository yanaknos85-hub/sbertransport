import { useGetExecutorGroups } from 'api/executor-group';
import React, {
  FC, Suspense, useEffect, useState
} from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { createPortal } from 'react-dom';
import { Pagination } from 'shared/components/Pagination';
import { Button } from 'shared/components/Button/Button';
import { useColumns } from './useColumns';
import { useModal, ModalProvider } from './context/modal.context';
import {
  StyledTableActions, TableStyled
} from './styled.table';
import { AddEditModal } from './AddEditModal';
import { Filters } from './Filter';
import { useExecutorSettingsContext } from './context/ExecutorSettings.context';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query,
  setPagination,
}) => {
  const [totalElements, setTotalElements] = useState(0);
  const executorGroupsData = useGetExecutorGroups(query);
  const { isLoading, data } = executorGroupsData;

  const columns = useColumns();
  const { openAdd } = useModal();
  const { footer } = useExecutorSettingsContext();

  const typedData = data as unknown as {
    data?: {
      content?: unknown[];
      totalElements?: number;
    };
  } | undefined;

  useEffect(() => {
    if (typedData?.data?.totalElements !== undefined) {
      setTotalElements(typedData.data.totalElements);
    }
  }, [typedData]);

  if (isLoading) {
    return <SpinWrapped />;
  }

  const tableData = typedData?.data?.content || [];

  return (
    <>
      <TableStyled
        columns={columns}
        dataSource={tableData}
        scroll={{ x: '100%' }}
        rowKey="id"
        loading={isLoading}
      />

      {footer.current && createPortal(
        <>
          <StyledTableActions>
            <Button type="primary" onClick={openAdd}>
              Добавить группу
            </Button>
          </StyledTableActions>
          <Pagination
            pagination={query}
            total={totalElements}
            setPagination={setPagination}
          />
        </>,
        footer.current!
      )}
    </>
  );
};

const ExecutorGroupsTable = () => {
  const {
    query, setPagination, setQuery,
  } = useQuery();

  return (
    <ModalProvider>
      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <Filters query={query} setQuery={setQuery} />
        </Suspense>
      </ErrorBoundary>

      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <Table
            query={query}
            setPagination={setPagination}
          />
        </Suspense>
      </ErrorBoundary>

      <AddEditModal />
    </ModalProvider>
  );
};

export default ExecutorGroupsTable;

