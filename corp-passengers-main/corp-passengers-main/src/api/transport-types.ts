import {
  GET_AVAILABLE_TRANSPORT_TYPES,
  GET_AVAILABLE_TRANSPORT_TYPES_BY_SERVICE_TYPE,
  GET_TRANSPORT_TYPES,
  SAVE_TRANSPORT_TYPES,
  TRANSPORT_SERVICE_TYPES,
  TRANSPORTTYPES,
  TRANSPORT_SERVICE_TYPES_CARGO
} from 'constants/constants.api';
import { OrganizationTransportTypes, TransportType } from 'stores/TransportTypes/TransportTypes.interface';
import * as t from 'io-ts';
import {
  APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import { MutationResultPair, QueryConfig } from 'react-query';
import { TransportServiceType } from 'stores/TransportServiceTypes/TransportServiceTypes.interface';

declare module 'api' {
  interface Cache {
    transportTypes: { key: ['transportTypes']; value: TransportType[] };
    organizationTransportTypes: { key: ['OrganizationTransportTypes', string]; value: OrganizationTransportTypes[] };
    transportServiceTypes: { key: ['transportServiceTypes']; value: TransportServiceType[] };
    availableTransportTypes: { key: ['availableTransportTypes', string]; value: TransportType[] };
    availableTransportTypesByServiceType: {
      key: ['availableTransportTypesByServiceType', string, string];
      value: TransportType[];
    };
  }
}

export const useTransportTypes = (
  config?: QueryConfig<TransportType[], Error>
): APIQueryResult<TransportType[], Error> => useAPI(
  ['transportTypes'],
  ({ http, process: { decodeResponseData } }) => (
    http.get<TransportType[]>(TRANSPORTTYPES).then(decodeResponseData(t.array(TransportType)))
  ),
  config
);

export const useOrganizationTransportTypes = (
  organizationId: string
): APIQueryResult<OrganizationTransportTypes[], Error> => useAPI(['OrganizationTransportTypes', organizationId], ({ http, process }) => http
  .get<OrganizationTransportTypes[]>(GET_TRANSPORT_TYPES, { urlParams: { organizationId } })
  .then(process.decodeResponseData(t.array(OrganizationTransportTypes)))
);
export const useGetAvailableTransportTypes = (
  organizationId: string,
  config?: QueryConfig<TransportType[], Error>
): APIQueryResult<TransportType[], Error> => useAPI(
  ['availableTransportTypes', organizationId],
  ({ http, process }) => http
    .get<TransportType[]>(GET_AVAILABLE_TRANSPORT_TYPES, { urlParams: { organizationId } })
    .then(process.decodeResponseData(t.array(TransportType))),
  config
);

export const useGetAvailableTransportTypesByServiceType = (
  organizationId: string,
  transportServiceTypeId: string
): APIQueryResult<TransportType[], Error> => useAPI(['availableTransportTypesByServiceType', organizationId, transportServiceTypeId], ({ http, process }) => http
  .get<TransportType[]>(GET_AVAILABLE_TRANSPORT_TYPES_BY_SERVICE_TYPE, {
    urlParams: { organizationId, transportServiceTypeId },
  })
  .then(process.decodeResponseData(t.array(TransportType)))
);

export const useSaveTransportTypes = (
  organizationId: string
): MutationResultPair<OrganizationTransportTypes[], unknown, OrganizationTransportTypes[], unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, transportTypes): Promise<any> => http.post<void>(`${SAVE_TRANSPORT_TYPES}/${organizationId}`, transportTypes).then(process.decodeResponseData()),
  {
    onSuccess: ({
      cache, variables: transportTypes, process, t,
    }) => {
      process.processStatus(200, t.crudMessages.saveSuccess);
      updateQueryCache(cache, ['OrganizationTransportTypes', organizationId], _ => transportTypes);
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.crudMessages.saveFailed);
    },
  }
);

export const useTransportServiceTypes = (
  config?: QueryConfig<TransportServiceType[], Error>
): APIQueryResult<TransportServiceType[], Error> => useAPI(
  ['transportServiceTypes'],
  ({ http, process: { decodeResponseData } }) => (
    http.get<TransportServiceType[]>(TRANSPORT_SERVICE_TYPES).then(decodeResponseData(t.array(TransportServiceType)))
  ),
  config
);
export const useTransportServiceTypesCargo = (
  config?: QueryConfig<TransportServiceType[], Error>
): APIQueryResult<TransportServiceType[], Error> => useAPI(
  ['transportServiceTypes'],
  ({ http, process: { decodeResponseData } }) => http
    .get<TransportServiceType[]>(TRANSPORT_SERVICE_TYPES_CARGO)
    .then(decodeResponseData(t.array(TransportServiceType))),
  config
);
