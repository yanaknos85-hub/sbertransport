import { useSearchContractors } from 'api/contractors/search';
import React, { FC, Suspense } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { createPortal } from 'react-dom';
import { Pagination } from 'shared/components/Pagination';
import { Button } from 'shared/components/Button/Button';
import { UploadButton, importExportEndpointMap } from 'modules/UploadButton';
import DownloadButton from 'components/DownloadButton';
import { useUploadContractors } from 'api/contractors';
import { ButtonLoad } from 'shared/components/ButtonLoad';
import { useColumns } from './useColumns';
import { useModal, ModalProvider } from './context/modal.context';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from '../../styled/styled.tariffs';
import { Filters } from './components/Filters';
import { AddEditModal } from './components/AddEditModal';
import { useTariffSettingsContext } from '../../context/TariffSettings.context';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query,
  setPagination,
}) => {
  const {
    page, size, ...filters
  } = query;

  const columns = useColumns();

  const { content, totalElements } = useSearchContractors({ pagination: { page, size }, ...filters }).data;

  const { openAdd } = useModal();

  const { footer } = useTariffSettingsContext();

  return (
    <>
      <TableStyled
        columns={columns}
        dataSource={content}
        scroll={{ x: '100%' }}
        rowKey="id"
      />

      {createPortal(
        <>
          <StyledTableActions>
            <Button type="primary" onClick={openAdd}>
              Добавить контрагента
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

const Contractors = () => {
  const {
    query, setPagination, setQuery,
  } = useQuery();

  return (
    <ModalProvider>
      <StyledHeader>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <Filters query={query} setQuery={setQuery} />
          </Suspense>
        </ErrorBoundary>

        <StyledRow>
          <DownloadButton
            url={`${importExportEndpointMap.contractor}/files/contractor`}
            customElement={<ButtonLoad type="import" />}
          />
          <UploadButton
            entity="contractor"
            useUpload={useUploadContractors}
            customElement={<ButtonLoad type="export" />}
          />
        </StyledRow>
      </StyledHeader>

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

export default Contractors;
