import React, {
  FC, Suspense, useEffect, useMemo, useRef
} from 'react';
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
import { useCargoContractsWithParams } from 'api/contracts';
import useColumns from '../hooks/useColumns';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/TariffSettings/styled/styled.tariffs';
import { ContractsSearchQuery } from 'stores/Contracts/Contracts.interface';
import { useCustomersContext } from '../../context/Customers.context';
import { useParams } from 'react-router-dom';
import { ContractTypes } from 'constants/constants.app';
import { processSearchContractsQuery } from '../utils/searchQuery';

import styles from './styles.module.scss';

interface TableProps {
  query: PaginationParams;
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { contractType } = useParams<{ contractType: ContractTypes }>();

  // Чтобы при смене contractType запрос не кидался заново, пока не обнулится query
  const fullQuery = useMemo(() => ({
    contractType,
    ...query,
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }), [query]);

  const {
    page, size, ...filters
  } = fullQuery;

  const { content, totalElements } = useCargoContractsWithParams({
    ...processSearchContractsQuery<ContractsSearchQuery>(filters),
    pagination: { page, size },
  }).data;

  const { incomeColumns, outcomeColumns } = useColumns();
  const { openAdd } = useModal();
  const { footer } = useCustomersContext();

  return (
    <>
      <TableStyled
        columns={contractType === ContractTypes.INCOME ? incomeColumns : outcomeColumns}
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

const defaultQuery = {
  active: 'true',
};

const ContractsTab = () => {
  const {
    query, setQuery, setPagination,
  } = useQuery<ContractsSearchQuery>(defaultQuery);

  const { contractType } = useParams<{ contractType: ContractTypes }>();

  const { organizationId } = useProfile().data;

  const isFirstRender = useRef(true);
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
    } else {
      setQuery(defaultQuery);
    }
  }, [contractType, setQuery]);

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
            url={`${importExportEndpointMap.tariffCargo}/files/contract/${organizationId}`}
            customElement={<ButtonLoad type="import" disabled />}
          />
          <UploadButton
            entity="contractCargo"
            useUpload={useUploadContractsCargo}
            customElement={<ButtonLoad type="export" disabled />}
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
