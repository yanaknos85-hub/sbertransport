import React, {
  FC, useEffect, MutableRefObject, useMemo, useState
} from 'react';
import { createPortal } from 'react-dom';
import moment from 'moment';
import { observer } from 'mobx-react';

import { useProfile } from 'api/profile';
import { useSearchGroupTransferRegistryDeffered, useSearchGroupTransferRegistryExecutorDeffered } from 'api/group-transfer-registry';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { RegistrySearchQuery, GroupTransferReportResponse } from 'stores/GroupTransferRegistry/GroupTransferRegistry';
import { useTable } from './hooks/useTable';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { useColumnVisibilitySettings } from './hooks/useColumnVisibilitySettings';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';

import { PageOptions, RequestBodyParamsGroupTransfer, TransportTypes } from './types/types';
import { processGroupTransferRequestBodyOnXlsDownload, processJournalData } from './utils/data';
import { TripRegistryColumnProps, useColumns } from './hooks/useColumns';
import { useSortingSettings } from './hooks/useSortingSettings';

export const GroupTransferRegistryTable: FC<{
  filterValues: RegistrySearchQuery;
  pageOptions: PageOptions;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  userId: string | undefined;
}> = observer(({
  filterValues, pageOptions, buttonsRef, userId,
}) => {
  const [data, setData] = useState<GroupTransferReportResponse>();
  const {
    organizationId, isOrganization, executorGroupId,
  } = useProfile().data;
  const { pageSetting, onPaginationChange } = pageOptions;
  const [prevPageSett, setPrevPageSett] = useState(pageSetting);
  const [prevFilter, setPrevFilter] = useState(filterValues);
  const [orgId, setOrgId] = useState(organizationId);
  const [isOrg, setIsOrg] = useState(isOrganization);
  const [execId, setExecId] = useState(executorGroupId);

  // @ts-ignore
  const [searchTransferRegister, { isLoading }] = useSearchGroupTransferRegistryDeffered(organizationId);
  // @ts-ignore
  const [
    searchTransferRegisterExecutor,
    { isLoading: isLoadingExecutor },
  ] = useSearchGroupTransferRegistryExecutorDeffered(executorGroupId);

  useEffect(() => {
    if (isOrganization) {
      searchTransferRegister({
        ...filterValues,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    } else if (executorGroupId?.length) {
      searchTransferRegisterExecutor({
        ...filterValues,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    }
  }, []);

  useEffect(() => {
    if (JSON.stringify(pageSetting) !== JSON.stringify(prevPageSett)
      || JSON.stringify(filterValues) !== JSON.stringify(prevFilter)
      || isOrganization !== isOrg
      || organizationId !== orgId
      || JSON.stringify(executorGroupId) !== JSON.stringify(execId)
    ) {
      if (isOrganization) {
        searchTransferRegister({
          ...filterValues,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else if (executorGroupId?.length) {
        searchTransferRegisterExecutor({
          ...filterValues,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else {
        setData(undefined);
      }
      setPrevPageSett(pageSetting);
      setPrevFilter(filterValues);
      setOrgId(organizationId);
      setExecId(executorGroupId);
      setIsOrg(isOrganization);
    }
  }, [filterValues, pageSetting, isOrganization, organizationId, executorGroupId]);

  const period = useMemo(
    () => [filterValues.desiredDateRange, filterValues.creationDate]
      .filter(Boolean)
      .map(range => ({
        start: moment(range?.start),
        end: moment(range?.end),
      })),
    [filterValues.desiredDateRange, filterValues.creationDate]
  );

  const {
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const { tableColumns } = useTable(userId);

  const {
    onDefaultSettings,
  } = useSortingSettings(userId);

  return (
    <>
      {buttonsRef
      && buttonsRef.current
      && createPortal(
        <ExportXLSModal<RegistrySearchQuery, Partial<RequestBodyParamsGroupTransfer>>
          transportType={TransportTypes.GROUP_TRANSFER.toLowerCase()}
          filterParams={filterValues}
          mimeType={XLSX_MIME_TYPE}
          onRequestParamsOnXlsDownload={processGroupTransferRequestBodyOnXlsDownload}
        />,
        buttonsRef.current
      )}

      <TableSettingsContainer>
        <DefaultSorting onClick={onDefaultSettings} />
        <DisplayPeriod period={period} />
        <ColumnVisibilitySettings
          setting={tableColumns}
          saveSettingChange={handleSaveColumnVisibilitySettings as (key?: Record<string, boolean | undefined>) => void}
          defaultColumns={useColumns()}
          transportType={TransportTypes.GROUP_TRANSFER.toLowerCase()}
        />
      </TableSettingsContainer>

      <Table
        isFetching={isOrganization ? isLoading : isLoadingExecutor}
        dataSource={data?.content ? processJournalData(data?.content) : []}
        columns={tableColumns as TripRegistryColumnProps[]}
        rowKey="id"
        pagination={{
          total: data?.totalElements,
          current: data?.number ? data?.number + 1 : 1,
          onChange: onPaginationChange,
          pageSize: data?.size,
        }}
      />
    </>
  );
});
