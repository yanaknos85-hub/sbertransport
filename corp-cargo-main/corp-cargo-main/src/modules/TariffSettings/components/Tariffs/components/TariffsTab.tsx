import React, { FC, Suspense } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';
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
import { TariffsHanbooksNames } from 'modules/NewTariffs/constants/Tariffs.constants';
import { useColumns } from '../hooks/useColumns';
import { useTariffs } from '../hooks/useTariffs';
import { tariffStatusOptions } from '../constants/constants';
import { AddEditModal } from './AddEditModal';
import { Filters } from './Filters';
import { useModal } from '../context/modal.context';
import {
  StyledHeader, StyledRow, StyledTableActions, TableStyled
} from 'modules/TariffSettings/styled/styled.tariffs';
import { TariffFilter } from 'stores/Tariffs/Tariffs.interface';
import { useTariffSettingsContext } from '../../../context/TariffSettings.context';
import { useProfile } from 'api/profile';

interface TableProps {
  query: PaginationParams & Record<string, any>; // TODO заменить на конкретный тип
  setPagination: (pagination: PaginationParams) => void;
}

const Table: FC<TableProps> = ({
  query, setPagination,
}) => {
  const { content, totalElements } = useTariffs({ ...query, isNightTariff: query.isNightTariff?.[0] }).data;

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
      />

      {createPortal(
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

const TariffsTab = () => {
  const {
    query, setQuery, setPagination,
  } = useQuery<TariffFilter>({
    [TariffsHanbooksNames.active]: tariffStatusOptions[0].value,
  });
  const { organizationId } = useProfile().data;

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
            url={`${importExportEndpointMap.tariffCargo}/files/tariff/?tariffId=${organizationId}`}
            customElement={<ButtonLoad type="import" />}
            params={filters}
          />
          <UploadButton
            entity="tariffCargo"
            useUpload={useUploadCargoTariffs('tariffCargo')}
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

export default TariffsTab;
