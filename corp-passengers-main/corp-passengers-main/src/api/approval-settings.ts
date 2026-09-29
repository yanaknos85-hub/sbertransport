import {
  updateQueryCache, useAPI, useAPIMutation, APIQueryResult
} from 'api';
import {
  APPROVALS_SETTINGS_PUBLIC,
  APPROVALS_SETTINGS_PUBLIC_UPDATE,
  APPROVALS_SETTINGS_TAXI,
  APPROVALS_SETTINGS_TAXI_UPDATE,
  APPROVALS_SETTINGS_OTHER_POST,
  APPROVALS_SETTINGS_OTHER_GET_PUT,
  APPROVALS_SETTINGS_TAXI_DEFAULT,
  APPROVALS_SETTINGS_PUBLIC_DEFAULT,
  APPROVALS_SETTINGS_OTHER_DEFAULT,
  APPROVALS_SETTINGS_GROUP_TRANSFER,
  APPROVALS_SETTINGS_GROUP_TRANSFER_UPDATE,
  APPROVALS_SETTINGS_GROUP_TRANSFER_DEFAULT
} from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import { ApprovalSettings, ApprovalSettingsQuery } from 'stores/ApprovalSettings/ApprovalSettings.interface';
import { UUID } from 'utils/io-ts';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

declare module 'api' {
  interface Cache {
    approvalsSettings: { key: ['approvalSettings']; value: { approvalSettings: ApprovalSettings } };
  }
}

const approvalsSettingsTaxiRaw2Cache = (approvalSettings: ApprovalSettings) => ({ approvalSettings });

// READ

export const useApprovalSettings = (
  orgId: UUID,
  transType: TransportTypes
): APIQueryResult<{ approvalSettings: ApprovalSettings }, unknown> => useAPI(['approvalSettings'], ({ http, process }) => {
  let url = '';
  switch (transType) {
    case TransportTypes.TAXI:
      url = APPROVALS_SETTINGS_TAXI;
      break;
    case TransportTypes.PUBLIC:
      url = APPROVALS_SETTINGS_PUBLIC;
      break;
    case TransportTypes.GROUP_TRANSFER:
      url = APPROVALS_SETTINGS_GROUP_TRANSFER;
      break;
    default:
      url = APPROVALS_SETTINGS_OTHER_GET_PUT;
      break;
  }
  return http
    .get<ApprovalSettings>(url, { urlParams: { orgId, transType } })
    .then(process.decodeResponseData(ApprovalSettings))
    .then(approvalsSettingsTaxiRaw2Cache)
    .catch(e => {
      if (e.response?.status === 404) {
        return { approvalSettings: {} as ApprovalSettings };
      }
      throw e;
    });
});

// CREATE

export const useSaveTaxiApprovalSettings = (): MutationResultPair<
  ApprovalSettingsQuery,
  unknown,
  { query: ApprovalSettingsQuery },
  unknown
> => useAPIMutation(
  ({ http, process }, { query }) => http
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_TAXI, ApprovalSettingsQuery.encode(query), {
      urlParams: { orgId: query.organizationId || '' },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery)),
  {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    onSuccess: ({ cache, result: appr }) => {
      // updateQueryCache(cache, [APPROVAL_SETTINGS_TAXI], apprs => [...apprs, { ...appr }]);
    },
  }
);

export const useSavePublicApprovalSettings = (): MutationResultPair<
  ApprovalSettingsQuery,
  unknown,
  { query: ApprovalSettingsQuery },
  unknown
> => useAPIMutation(
  ({ http, process }, { query }) => http
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_PUBLIC, ApprovalSettingsQuery.encode(query), {
      urlParams: { orgId: query.organizationId as UUID },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery)),
  {
    onSuccess: ({
      cache, process, result: appr,
    }) => {
      process.processStatus(200, 'Данные по согласованиям для общественного транспорта успешно созданы');
      updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
    },
  }
);

export const useSaveGroupTransferApprovalSettings = (): MutationResultPair<
  ApprovalSettingsQuery,
  unknown,
  { query: ApprovalSettingsQuery },
  unknown
> => useAPIMutation(
  ({ http, process }, { query }) => http
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_GROUP_TRANSFER, ApprovalSettingsQuery.encode(query), {
      urlParams: { orgId: query.organizationId as UUID },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery)),
  {
    onSuccess: ({
      cache, process, result: appr,
    }) => {
      process.processStatus(200, 'Данные по согласованиям для трансфера успешно созданы');
      updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
    },
  }
);

export const useSaveOtherApprovalSettings = (): MutationResultPair<
  ApprovalSettingsQuery,
  unknown,
  { query: ApprovalSettingsQuery },
  unknown
> => useAPIMutation(
  ({ http, process }, { query }) => http
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_OTHER_POST, ApprovalSettingsQuery.encode(query), {
      urlParams: { orgId: query.organizationId as UUID },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery)),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, process, result: appr,
    }) => {
      process.processStatus(200, 'Данные по согласованиям для выбранного вида транспорта успешно созданы');
      // updateQueryCache(cache, [APPROVAL_SETTINGS_PUBLIC], apprs => [...apprs, { ...appr }]);
    },
  }
);

// UPDATE

export const useUpdateTaxiApprovalSettings = (
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery, unknown, ApprovalSettingsQuery, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http
    .put<void>(APPROVALS_SETTINGS_TAXI_UPDATE, ApprovalSettingsQuery.encode(settingsAttribute), {
      urlParams: { apprId: apprId || '', orgId: settingsAttribute.organizationId as UUID },
    })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные по согласованиям для такси успешно обновлены');
      cache.refetchQueries(['approvalSettings']);
    },
  }
);

export const useUpdatePublicApprovalSettings = (
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery, unknown, ApprovalSettingsQuery, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http
    .put<void>(APPROVALS_SETTINGS_PUBLIC_UPDATE, ApprovalSettingsQuery.encode(settingsAttribute), {
      urlParams: { orgId: settingsAttribute.organizationId as UUID, apprId: apprId || '' },
    })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные по согласованиям для общественного транспорта успешно обновлены');
      cache.refetchQueries(['approvalSettings']);
    },
  }
);

export const useUpdateGroupTransferApprovalSettings = (
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery, unknown, ApprovalSettingsQuery, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http
    .put<void>(APPROVALS_SETTINGS_GROUP_TRANSFER_UPDATE, ApprovalSettingsQuery.encode(settingsAttribute), {
      urlParams: { orgId: settingsAttribute.organizationId as UUID, apprId: apprId || '' },
    })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные по согласованиям для трансфера успешно обновлены');
      cache.refetchQueries(['approvalSettings']);
    },
  }
);

export const useUpdateOtherApprovalSettings = (
  orgId: UUID,
  transType: TransportTypes
): MutationResultPair<ApprovalSettingsQuery, unknown, ApprovalSettingsQuery, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http
    .put<void>(APPROVALS_SETTINGS_OTHER_GET_PUT, ApprovalSettingsQuery.encode(settingsAttribute), {
      urlParams: { orgId, transType },
    })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, 'Данные по согласованиям для выбранного вида транспорта успешно обновлены');
      cache.refetchQueries(['approvalSettings']);
    },
  }
);

// SET DEFAULTS

export const useDefaultTaxiApprovalSettings = (
  orgId: UUID,
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery | null, unknown, unknown, unknown> => useAPIMutation(
  ({ http, process }) => http
  // @ts-ignore
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_TAXI_DEFAULT, undefined, {
      urlParams: { orgId, apprId: apprId || '' },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery))
    .catch(e => {
      if (e.response.status === 404 || e.response.status === 405) {
        process.processStatus(
          e.response.status,
          'Невозможно установить по умолчанию данные, которые ни разу не создавались'
        );

        return null;
      }

      throw e;
    }),
  {
    onSuccess: ({ cache, result: appr }) => {
      updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
    },
  }
);

export const useDefaultPublicApprovalSettings = (
  orgId: UUID,
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery | null, unknown, unknown, unknown> => useAPIMutation(
  ({ http, process }) => http
  // @ts-ignore
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_PUBLIC_DEFAULT, undefined, {
      urlParams: { orgId, apprId: apprId || '' },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery))
    .catch(e => {
      if (e.response.status === 404 || e.response.status === 405) {
        process.processStatus(
          e.response.status,
          'Невозможно установить по умолчанию данные, которые ни разу не создавались'
        );

        return null;
      }

      throw e;
    }),
  {
    onSuccess: ({ cache, result: appr }) => {
      updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
    },
  }
);

export const useDefaultGroupTransferApprovalSettings = (
  orgId: UUID,
  apprId?: string | null
): MutationResultPair<ApprovalSettingsQuery | null, unknown, unknown, unknown> => useAPIMutation(
  ({ http, process }) => http
    // @ts-ignore
    .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_GROUP_TRANSFER_DEFAULT, undefined, {
      urlParams: { orgId, apprId: apprId || '' },
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery))
    .catch(e => {
      if (e.response.status === 404 || e.response.status === 405) {
        process.processStatus(
          e.response.status,
          'Невозможно установить по умолчанию данные, которые ни разу не создавались'
        );

        return null;
      }

      throw e;
    }),
  {
    onSuccess: ({ cache, result: appr }) => {
      updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
    },
  }
);

export const useDefaultOtherApprovalSettings = (
  orgId: UUID,
  transType: TransportTypes
): MutationResultPair<ApprovalSettingsQuery | null, unknown, { query: ApprovalSettingsQuery }, unknown> => (
  useAPIMutation(
    ({ http, process }) => http
    // @ts-ignore
      .post<ApprovalSettingsQuery>(APPROVALS_SETTINGS_OTHER_DEFAULT, undefined, {
        urlParams: { orgId, transType },
      })
      .then(process.decodeResponseData(ApprovalSettingsQuery))
      .catch(e => {
        if (e.response.status === 404 || e.response.status === 405) {
          process.processStatus(
            e.response.status,
            'Невозможно установить по умолчанию данные, которые ни разу не создавались'
          );

          return null;
        }

        throw e;
      }),
    {
      onSuccess: ({ cache, result: appr }) => {
        updateQueryCache(cache, ['approvalSettings'], () => approvalsSettingsTaxiRaw2Cache(appr as ApprovalSettings));
      },
    }
  )
);
