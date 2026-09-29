import { useGetExecutorGroupsDef } from 'api/executor-group';
import React, {
  FC, Suspense, useEffect, useLayoutEffect, useState
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
  const [content, setContent] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [queryParams, setQueryParams] = useState(query);

  const columns = useColumns();

  const [getExecutorGroups, { isLoading }] = useGetExecutorGroupsDef(query);

  const { openAdd } = useModal();

  const { footer } = useExecutorSettingsContext();

  useLayoutEffect(() => {
    getExecutorGroups(query).then(data => {
      // @ts-ignore
      setContent(data?.data?.content);
      // @ts-ignore
      setTotalElements(data?.data?.totalElements);
    });
  }, []);

  useEffect(() => {
    if (JSON.stringify(query) !== JSON.stringify(queryParams)) {
      setQueryParams(query);
      getExecutorGroups(query).then(data => {
        // @ts-ignore
        setContent(data?.data?.content);
        // @ts-ignore
        setTotalElements(data?.data?.totalElements);
      });
    }
  }, [query]);

  return (
    <>
      <TableStyled
        columns={columns}
        dataSource={content}
        scroll={{ x: '100%' }}
        rowKey="id"
        loading={isLoading}
      />

      {createPortal(
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
        footer.current
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

