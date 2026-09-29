import React, {
  FC, Suspense, useEffect, useRef, useState, useMemo
} from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { useParams } from 'react-router-dom';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useQuery } from 'shared/hooks/useQuery';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { createPortal } from 'react-dom';
import { Button } from 'shared/components/Button/Button';
import { Pagination } from 'shared/components/Pagination';
import { importExportEndpointMap, UploadButton } from 'modules/UploadButton';
import DownloadButton from 'components/DownloadButton';
import { useUploadCargoTariffs } from 'api/tariffs-cargo';
import { ButtonLoad } from 'shared/components/ButtonLoad';
import { useColumns } from '../hooks/useColumns';
import { useTariffs } from '../hooks/useTariffs';
import { AddEditModal } from './AddEditModal';
import { Filters } from './Filters';
import { useModal } from '../context/modal.context';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/TariffSettings/styled/styled.tariffs';
import { TariffFilter } from 'stores/Tariffs/Tariffs.interface';
import { useCustomersContext } from '../../context/Customers.context';
import { useProfile } from 'api/profile';
import { TariffTypes } from 'constants/constants.app';

interface TableProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  query: PaginationParams & Record<string, any>; // TODO заменить на конкретный тип
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { content, totalElements } = useTariffs({
    ...query, contractType: query.contractType, isNightTariff: query.isNightTariff?.[0],
  }).data;

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
        dataSource={content}
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
            total={totalElements}
            setPagination={setPagination}
          />
        </>,
        footer.current
      )}
    </>
  );
};

const defaultFilters = {
  active: 'true',
};

const TariffsTab = () => {
  const {
    query, setQuery, setPagination,
  } = useQuery<TariffFilter>(defaultFilters);

  const { organizationId } = useProfile().data;
  const { tariffType } = useParams<{ tariffType: TariffTypes }>();

  const { page, ...filters } = query;

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
    contractType: tariffType,
    ...query,
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }), [tariffType, query]);

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
            url={`${importExportEndpointMap.tariffCargo}/files/tariff/${organizationId}`}
            customElement={<ButtonLoad type="import" disabled />}
            params={filters}
          />
          <UploadButton
            entity="tariffCargo"
            useUpload={useUploadCargoTariffs('tariffCargo')}
            customElement={<ButtonLoad type="export" disabled />}
          />
        </StyledRow>
      </StyledHeader>

      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <Table
            query={fullQuery}
            setPagination={setPagination}
          />
        </Suspense>
      </ErrorBoundary>

      <AddEditModal />
    </div>
  );
};

export default TariffsTab;
