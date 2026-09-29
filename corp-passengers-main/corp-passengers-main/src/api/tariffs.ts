import { ILogger } from '@sber-sbertransport/mf-core';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { AxiosResponse } from 'axios';
import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';

import {
  CREATE_TARIFF,
  DELETE_TARIFF,
  GET_ALL_REPORTS,
  GET_ALL_TARIFFS,
  GET_BICYCLE_TARIFF,
  GET_CARSHARING_TARIFF,
  GET_COURIER_TARIFF,
  GET_DEDICATED_TARIFF,
  GET_DOMESTIC_COURIER_TARIFF,
  GET_GROUP_TRANSFER_TARIFF,
  GET_INTERREGIONAL_TARIFF,
  GET_PERSONAL_TARIFF,
  GET_PUBLIC_TARIFF,
  GET_SCOOTER_TARIFF,
  GET_TARIFF,
  GET_TARIFF_TRANSPORT,
  GET_TAXI_TARIFF,
  SEARCH
} from '../constants/constants.api';
import { importExportEndpointMap } from '../modules/UploadButton';
import {
  TariffsSearchResponse,
  RegionInfoType,
  Tariff,
  TariffFilter,
  TariffJson,
  TariffsInfo,
  TariffTransportType,
  TariffTransportParams,
  TariffTransport,
  TariffTransportResponse
} from 'stores/Tariffs/Tariffs.interface';
import { TransportTypes } from '../stores/TransportTypes/TransportTypes.interface';
import { UUID } from '../utils/io-ts';
import { mkUseUploadEntity } from './upload';

enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}

declare module 'api' {
  interface Cache {
    tariffs: {
      key: ['tariffs'];
      value: Tariff[];
    };
    filteredTariffs: {
      key: ['filteredTariffs', TariffFilter];
      value: TariffsInfo;
    };
    tariffTransport: {
      key: ['tariffTransport', Partial<TariffTransportParams>];
      value: TariffTransportType[];
    };
    tariff: {
      key: ['tariff', UUID];
      value: TariffJson;
    };
    RegionInfoType: { key: ['RegionInfoType']; value: RegionInfoType[] };
    transportTypeTariff: {
      key: ['transportTypeTariff', UUID | null | undefined, keyof typeof TransportTypes];
      value: TariffJson | null;
    };
    tariffsSearch: {
      key: ['tariffsSearch'];
      value: Tariff[];
    };
  }
}

interface Error {
  request: {
    response: string;
    status?: number;
  };
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const errorHandler = (error: Error, t: any, logger: ILogger) => {
  const response = JSON.parse(error?.request?.response);
  switch (error?.request?.status) {
    case 500:
      response?.message !== ''
        ? logger.toMessage('error', response.message)
        : logger.toMessage('error', t.Tariffs.InternalServerError);
      break;
    case 409:
      response?.message !== ''
        ? logger.toMessage('error', response.message)
        : logger.toMessage('error', t.Tariffs.duplicate);
      break;
    case 400:
      response?.message !== ''
        ? logger.toMessage('error', response.message)
        : logger.toMessage('error', t.Tariffs.BadRequest);
      break;
    default:
      break;
  }
};

export const useTariffs = (): APIQueryResult<Tariff[]> => useAPI(['tariffs'], ({ http, process }) => http.get<Tariff[]>(GET_ALL_TARIFFS).then(process.decodeResponseData(t.array(Tariff)))
);

export const useFilteredTariffs = (
  params: TariffFilter,
  config?: QueryConfig<TariffsInfo>
): APIQueryResult<TariffsInfo> => useAPI(
  ['filteredTariffs', params],
  ({ http, process }) => http
    .post<TariffsInfo>(`${GET_ALL_TARIFFS}${SEARCH}?size=${params.page.pageSize}&page=${params.page.pageNumber}`, {
      ...params,
    })
    .then(process.decodeResponseData(TariffsInfo)),
  config
);

// @ts-ignore; TODO: Временно, поправить типизацию
export const useTariffTransport = (params: TariffTransportParams, config: QueryConfig<TariffTransportResponse>): APIQueryResult<TariffTransportResponse> => useAPI(['tariffTransport', params], ({ http, process }) => http.get<TariffTransportResponse>(GET_TARIFF_TRANSPORT, { params }).then(process.decodeResponseData(t.array(TariffTransport))), config);

export const useCreateTariff = () => useAPIMutation(
  ({ http, process }, { tariff, transTypeId }: {
    tariff: TariffJson; transTypeId: string; refetchTariffs: () => void;
  }) => http
    .post(CREATE_TARIFF, TariffJson.encode(tariff), { urlParams: { transTypeId } })
  // @ts-ignore
    .then<TariffJson>(process.getResponseData),
  {
    onSuccess: ({
      cache, process, t, variables,
    }) => {
      process.processStatus(200, t.Tariffs.tariffAddSuccess);
      cache.refetchQueries(['tariffs']);
      variables.refetchTariffs();
    },
    onError: ({
      error, t, logger,
    }) => errorHandler(error, t, logger),
  }
);

export const useUpdateTariff = () => useAPIMutation(
  ({ http }, {
    tariff, transTypeId, tariffId,
  }: {
    tariff: TariffJson;
    transTypeId: string;
    tariffId: string;
    refetchTariffs: () => void;
  }) => (
    http.put(GET_TARIFF, TariffJson.encode(tariff), { urlParams: { transTypeId, tariffId } })
  ),
  {
    onSuccess: ({
      cache, t, process, variables,
    }) => {
      process.processStatus(200, t.Tariffs.tariffEditSuccess);
      cache.refetchQueries(['tariffs']);
      variables.refetchTariffs();
      cache.refetchQueries(['tariff', variables.tariffId]);
    },
    onError: ({
      error, t, logger,
    }) => errorHandler(error, t, logger),
  }
);

export const useTariff = (
  {
    transTypeId,
    tariffId,
  }: {
    transTypeId: string;
    tariffId: UUID;
  },
  config?: QueryConfig<TariffJson, Error>
): APIQueryResult<TariffJson, Error> => useAPI(
  ['tariff', tariffId],
  ({ http, process }) => http
    .get<TariffJson>(GET_TARIFF, { urlParams: { transTypeId, tariffId } })
    .then(process.decodeResponseData(TariffJson)),
  config
);

export const useDeleteTariff = (): MutationResultPair<
  AxiosResponse<number>,
  unknown,
  { transTypeId: string; tariffId: UUID; refetchTariffs: () => void },
  unknown
> => useAPIMutation(
  ({ http }, { transTypeId, tariffId }: { transTypeId: string; tariffId: UUID; refetchTariffs: () => void }) => (
    http.delete<number>(DELETE_TARIFF, { urlParams: { transTypeId, tariffId } })
  ),
  {
    onSuccess: ({
      cache, process, t, variables,
    }) => {
      process.processStatus(200, t.Tariffs.tariffDeleteSuccess);
      cache.refetchQueries(['tariffs']);
      variables.refetchTariffs();
      cache.refetchQueries(['tariff', variables.tariffId]);
    },
    onError: ({ t, logger }) => {
      logger.toMessage('error', t.Tariffs.InternalServerError);
    },
  }
);

export const useGetListRegions = (
  config?: QueryConfig<RegionInfoType[], Error>
): APIQueryResult<RegionInfoType[], Error> => useAPI(
  ['RegionInfoType'],
  ({ http, process: { decodeResponseData } }) => http.get<RegionInfoType[]>(`/geo-zones/`).then(decodeResponseData(t.array(RegionInfoType))),
  config
);

export const useUploadTariffs = (
  entity: keyof typeof importExportEndpointMap
) => mkUseUploadEntity(entity, {
  onSuccess: ({ cache }) => {
    cache.refetchQueries(['tariffs']);
    cache.refetchQueries(['import-report', entity]);
  },
});

export const useTransportTypeTariff = (
  tariffId: UUID | null | undefined,
  transportType: keyof typeof TransportTypes
): APIQueryResult<TariffJson | null> => useAPI(['transportTypeTariff', tariffId, transportType], ({ http, process }) => tariffId
  ? {
    PUBLIC: () => http.get<TariffJson>(GET_PUBLIC_TARIFF, { urlParams: { tariffId } }),
    TAXI: () => http.get<TariffJson>(GET_TAXI_TARIFF, { urlParams: { tariffId } }),
    PERSONAL: () => http.get<TariffJson>(GET_PERSONAL_TARIFF, { urlParams: { tariffId } }),
    CARSHARING: () => http.get<TariffJson>(GET_CARSHARING_TARIFF, { urlParams: { tariffId } }),
    BICYCLE: () => http.get<TariffJson>(GET_BICYCLE_TARIFF, { urlParams: { tariffId } }),
    WALK: () => http.get<TariffJson>(GET_PUBLIC_TARIFF, { urlParams: { tariffId } }),
    SCOOTER: () => http.get<TariffJson>(GET_SCOOTER_TARIFF, { urlParams: { tariffId } }),
    DEDICATED: () => http.get<TariffJson>(GET_DEDICATED_TARIFF, { urlParams: { tariffId } }),
    INDIVIDUAL: () => http.get<TariffJson>(GET_DEDICATED_TARIFF, { urlParams: { tariffId } }),
    COURIER: () => http.get<TariffJson>(GET_COURIER_TARIFF, { urlParams: { tariffId } }),
    INTERREGIONAL: () => http.get<TariffJson>(GET_INTERREGIONAL_TARIFF, { urlParams: { tariffId } }),
    DOMESTIC_COURIER: () => http.get<TariffJson>(GET_DOMESTIC_COURIER_TARIFF, { urlParams: { tariffId } }),
    GROUP_TRANSFER: () => http.get<TariffJson>(GET_GROUP_TRANSFER_TARIFF, { urlParams: { tariffId } }),
  }[transportType]().then(process.decodeResponseData(TariffJson))
  : null
);

export const useSelectTariffsSearch = (
  organizationId: UUID
): MutationResultPair<
  TariffsSearchResponse,
  Error,
  { page: number; humanReadableId: string; transportType: keyof typeof TransportTypesPassenger },
  unknown
> => useAPIMutation(
  async ({ http, process }, {
    page, humanReadableId, transportType,
  }) => http
    .post(GET_ALL_REPORTS, {
      humanReadableId, organizationId, transportType,
    }, { params: { size: 20, page } })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['tariffsSearch']);
    },
  }
);
