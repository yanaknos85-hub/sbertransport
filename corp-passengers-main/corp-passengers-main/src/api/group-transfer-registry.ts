import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import {
  GroupTransferReportResponse,
  GroupTransferReportItem,
  RegistrySearchQuery
} from 'stores/GroupTransferRegistry/GroupTransferRegistry';
import { MutationResultPair, QueryConfig } from 'react-query';
import { GROUP_TRANSFER_REPORT, GROUP_TRANSFER_REPORT_EXECUTOR } from 'constants/constants.api';

declare module 'api' {
  interface Cache {
    groupTransferRegistry: { key: ['groupTransferRegistry', RegistrySearchQuery]; value: GroupTransferReportResponse };
    singleRequest: { key: ['singleRequest', string]; value: GroupTransferReportItem };
  }
}

export const useSearchGroupTransferRegistry = (
  query: RegistrySearchQuery,
  config: QueryConfig<GroupTransferReportResponse, unknown>,
  orgId: string | null | undefined
): APIQueryResult<GroupTransferReportResponse, unknown> => useAPI(
  ['groupTransferRegistry', query],
  ({ http, process }) => orgId
    ? http
      .post<GroupTransferReportResponse>(
        GROUP_TRANSFER_REPORT,
        { ...query, organizationId: orgId },
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(GroupTransferReportResponse))
    : ({} as GroupTransferReportResponse),
  config
);

export const useSearchGroupTransferRegistryDeffered = (
  orgId: string | null | undefined
): MutationResultPair<RegistrySearchQuery, unknown, RegistrySearchQuery, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => orgId
    ? http
      .post<GroupTransferReportResponse>(
        GROUP_TRANSFER_REPORT,
        {
          ...query, organizationId: orgId,
        },
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(GroupTransferReportResponse))
    : ({} as GroupTransferReportResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(GroupTransferReportResponse);
    },
  }
);

export const useSearchGroupTransferRegistryExecutorDeffered = (
  execId?: string[] | undefined
): MutationResultPair<RegistrySearchQuery, unknown, RegistrySearchQuery, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => (execId)
    ? http
      .post<GroupTransferReportResponse>(
        GROUP_TRANSFER_REPORT_EXECUTOR,
        {
          ...query, executorGroupIds: execId,
        }
      )
      .then(process.decodeResponseData(GroupTransferReportResponse))
    : ({} as GroupTransferReportResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(GroupTransferReportResponse);
    },
  }
);

export const useSearchGroupTransferRegistryExecutor = (
  query: RegistrySearchQuery,
  config: QueryConfig<GroupTransferReportResponse, unknown>,
  orgId: string | null | undefined
): APIQueryResult<GroupTransferReportResponse, unknown> => useAPI(
  ['groupTransferRegistry', query],
  ({ http, process }) => orgId
    ? http
      .post<GroupTransferReportResponse>(
        GROUP_TRANSFER_REPORT_EXECUTOR,
        { ...query, organizationId: orgId },
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(GroupTransferReportResponse))
    : ({} as GroupTransferReportResponse),
  config
);
