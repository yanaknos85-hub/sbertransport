import React, { FC, Suspense } from 'react';
import { useProfile } from 'api/profile';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { createPortal } from 'react-dom';
import { Button } from 'shared/components/Button/Button';
import { Pagination } from 'shared/components/Pagination';
import DownloadButton from 'components/DownloadButton';
import { UploadButton, importExportEndpointMap } from 'modules/UploadButton';
import { ButtonLoad } from 'shared/components/ButtonLoad';
import { useUploadContractsCargo } from 'api/contracts';
import { AddEditModal } from './AddEditModal';
import { Filters } from './Filters';
import { useModal } from '../context/modal.context';
import { useContracts } from '../hooks/useContracts';
import useColumns from '../hooks/useColumns';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/TariffSettings/styled/styled.tariffs';
import { ContractsSearchQuery } from 'stores/Contracts/Contracts.interface';
import { useTariffSettingsContext } from '../../../context/TariffSettings.context';

import styles from './styles.module.scss';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { content, totalElements } = useContracts(query).data;
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
  const { organizationId } = useProfile().data;

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
            url={`${importExportEndpointMap.tariffCargo}/files/contract`}
            customElement={<ButtonLoad type="import" />}
            params={{ organizationId }}
          />
          <UploadButton
            entity="contractCargo"
            useUpload={useUploadContractsCargo}
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
    </div>
  );
};

export default ContractsTab;
