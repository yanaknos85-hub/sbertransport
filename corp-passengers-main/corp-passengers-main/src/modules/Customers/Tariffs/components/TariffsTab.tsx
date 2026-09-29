import React, {
  FC, Suspense, useCallback, useEffect, useMemo, useRef, useState
} from 'react';
import { useParams } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { createPortal } from 'react-dom';
import { Button } from 'shared/components/Button/Button';
import { Pagination } from 'shared/components/Pagination';
import { importExportEndpointMap, UploadButton } from 'modules/UploadButton';
import DownloadButton from 'components/DownloadButton';
import { useUploadTariffs } from 'api/tariffs';
import { ButtonLoad } from 'shared/components/ButtonLoad';
import { TariffsHanbooksNames } from 'modules/NewTariffs/constants/Tariffs.constants';
import { useColumns } from '../hooks/useColumns';
import { tariffStatusOptions } from '../constants/constants';
import { AddEditModal } from './AddEditModal';
import { Filters } from './Filters';
import { useModal } from '../context/modal.context';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/Customers/styled/styled.customers';
import { TariffFilter } from 'stores/Tariffs/Tariffs.interface';
import { observer } from 'mobx-react';
import { StoreNames, useAppStore } from 'stores';
import { useCustomersContext } from 'modules/Customers/context/Customers.context';
import { TariffTypes } from 'constants/constants.app';

interface TableProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  query: PaginationParams & Record<string, any>; // TODO заменить на конкретный тип
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = observer(({
  query, setPagination,
}) => {
  const { [StoreNames.tariffsStore]: tariffsStore } = useAppStore();

  const columns = useColumns();

  const { openAdd } = useModal();
  const { footer } = useCustomersContext();

  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  return (
    <>
      <TableStyled
        columns={columns}
        dataSource={tariffsStore.filteredTariffs}
        scroll={{ x: '100%' }}
        rowKey="id"
      />

      {isMounted && createPortal(
        <>
          <StyledTableActions>
            <Button type="primary" onClick={openAdd}>
              Добавить тариф
            </Button>
          </StyledTableActions>
          <Pagination
            pagination={query}
            total={tariffsStore.totalElements}
            setPagination={setPagination}
          />
        </>,
        footer.current
      )}
    </>
  );
});

const defaultFilters = {
  [TariffsHanbooksNames.active]: tariffStatusOptions[0].value,
};

const TariffsTab = observer(() => {
  const {
    query, setQuery, setPagination,
  } = useQuery<TariffFilter>(defaultFilters);

  const { tariffType } = useParams<{ tariffType: TariffTypes }>();

  const { [StoreNames.tariffsStore]: tariffsStore } = useAppStore();

  const handleRefetchTariffs = useCallback(() => {
    tariffsStore.refetchSDOTariffs(tariffType);
  }, [tariffsStore, tariffType]);

  const isFirstRender = useRef(true);
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
    } else {
      setQuery(defaultFilters);
    }
  }, [tariffType, setQuery]);

  // Чтобы при смене tariffType запрос не кидался заново, пока не обнулится query
  const fullQuery = useMemo(() => ({
    tariffType,
    ...query,
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }), [query]);

  useEffect(() => {
    const {
      page: pageNumber, size: pageSize, ...tariffFilters
    } = fullQuery;

    tariffsStore.setTariffsFilters({
      page: {
        pageNumber,
        pageSize,
      },
      ...tariffFilters,
      organizationId: fullQuery.tariffType === TariffTypes.INCOME ? tariffFilters.organizationId : undefined,
      contractorId: fullQuery.tariffType === TariffTypes.OUTCOME ? tariffFilters.contractorId : undefined,
      isNightTariff: tariffFilters.isNightTariff?.[0],
    });

    tariffsStore.getSDOTariffs(fullQuery.tariffType);
  }, [fullQuery, tariffsStore]);

  const { page, ...filters } = query;
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
            url={`${importExportEndpointMap.tariff}/files/tariff`}
            customElement={<ButtonLoad type="import" disabled />}
            params={filters}
          />
          <UploadButton
            entity="tariff"
            useUpload={useUploadTariffs('tariff')}
            customElement={<ButtonLoad type="export" disabled />}
            onSuccessReport={handleRefetchTariffs}
          />
        </StyledRow>
      </StyledHeader>

      <ErrorBoundary>
        {tariffsStore.isLoading
          ? <SpinWrapped />
          : <Table query={query} setPagination={setPagination} />}
      </ErrorBoundary>

      <AddEditModal />
    </div>
  );
});

export default TariffsTab;
