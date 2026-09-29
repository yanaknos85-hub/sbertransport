import React, { FC, Suspense } from 'react';
import { createPortal } from 'react-dom';

// import { useUploadContracts } from 'api/contracts';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Button } from 'shared/components/Button/Button';
import { Pagination } from 'shared/components/Pagination';
import { ButtonLoad } from 'shared/components/ButtonLoad';

import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { ContractsSearchQuery } from 'stores/Contracts/Contracts.interface';
import DownloadButton from 'components/DownloadButton';
import { importExportEndpointMap } from 'modules/UploadButton';

import { AddEditModal } from './AddEditModal';
import { Filters } from './Filters';
import { useModal } from '../context/modal.context';
import { useContracts } from '../hooks/useContracts';
import useColumns from '../hooks/useColumns';

import { useTariffSettingsContext } from '../../../context/TariffSettings.context';
import { processSearchContractsQuery } from '../utils/searchQuery';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/TariffSettings/styled/styled.tariffs';

import styles from './styles.module.scss';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { content, totalElements } = useContracts(processSearchContractsQuery(query)).data;
  const columns = useColumns();
  const { openAdd } = useModal();
  const { footer } = useTariffSettingsContext();

  return (
    <>
      <TableStyled
        columns={columns}
        dataSource={content}
        scroll={{ x: '100%' }}
        rowKey="id"
        className={styles.contractsTable}
      />

      {createPortal(
        <>
          <StyledTableActions>
            <Button type="primary" onClick={openAdd}>
              Добавить договор
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

const ContractsTab = () => {
  const {
    query, setQuery, setPagination,
  } = useQuery<ContractsSearchQuery>({
    active: 'true',
  });

  return (
    <div>
      <StyledHeader>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <Filters
              query={query}
              setQuery={setQuery}
            />
          </Suspense>
        </ErrorBoundary>

        <StyledRow>
          <DownloadButton
            url={`${importExportEndpointMap.contract}/files/contract`}
            customElement={<ButtonLoad type="import" />}
          />
          {/* Скрыто до исправления работоспособности на беке */}
          {/* <UploadButton */}
          {/*  entity="contract" */}
          {/*  useUpload={useUploadContracts} */}
          {/*  customElement={<ButtonLoad type="export" />} */}
          {/* /> */}
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
    </div>
  );
};

export default ContractsTab;
