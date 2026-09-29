import { ILogger } from '@sber-sbertransport/mf-core';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { AxiosResponse, AxiosError } from 'axios';
import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';

import {
  CREATE_TARIFF_CARGO,
  DELETE_TARIFF_CARGO,
  GET_ALL_GEO_ZONES,
  GET_ALL_TARIFFS_CARGO,
  GET_COURIER_TARIFF_CARGO,
  GET_DEDICATED_TARIFF_CARGO,
  GET_DOMESTIC_COURIER_TARIFF_CARGO,
  GET_INTERREGIONAL_TARIFF_CARGO,
  GET_TARIFF_CARGO,
  SEARCH,
  GET_AUTO_GUIDE,
  GET_PACK_CARGO,
  GET_ALL_TARIFFS_PACK,
  GET_TARIFF_PACK, TARIFF_PACK_BY_ID,
  GET_INDIVIDUAL_TARIFF_CARGO
} from '../constants/constants.api';
import { importExportEndpointMap } from '../modules/UploadButton';
import {
  AutoGuide,
  RegionInfoType,
  Tariff,
  TariffFilter,
  TariffJson,
  TariffPackInfo,
  TariffsInfo,
  TariffPack,
  PackData,
  CargoPackPost,
  CargoPackPut
} from '../stores/Tariffs/Tariffs.interface';
import { TransportTypesCargo } from '../stores/TransportTypes/TransportTypes.interface';
import { UUID } from '../utils/io-ts';
import { mkUseUploadCargoEntity, mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    tariffsCargo: {
      key: ['tariffsCargo'];
      value: Tariff[];
    };
    filteredTariffsCargo: {
      key: ['filteredTariffsCargo', TariffFilter];
      value: TariffsInfo;
    };
    tariffCargo: {
      key: ['tariffCargo', UUID];
      value: TariffJson;
    };
    autoGuide: {
      key: ['autoGuide', string];
      value: AutoGuide[];
    };
    pack: {
      key: ['pack'];
      value: PackData;
    };
    tariffsPack: {
      key: ['tariffsPack'];
      value: TariffPackInfo;
    };
    tariffPack: {
      key: ['tariffPack', string | null | undefined];
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      value: TariffPack | any;
    };
    RegionInfoTypeCargo: { key: ['RegionInfoTypeCargo']; value: RegionInfoType[] };
    transportTypeTariffCargo: {
      key: ['transportTypeTariffCargo', UUID | null | undefined, keyof typeof TransportTypesCargo];
      value: TariffJson | null;
    };
  }
}

interface TariffErrorMessages {
  Tariffs: {
    InternalServerError: string;
    duplicate: string;
    BadRequest: string;
  };
}

interface Error {
  request: {
    response: string;
    status?: number;
  };
}

const errorHandler = (error: Error, t: TariffErrorMessages, logger: ILogger) => {
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

// deprecated
export const useTariffs = (): APIQueryResult<Tariff[]> => useAPI(['tariffsCargo'], ({
  http, process,
}) => http.get<Tariff[]>(GET_ALL_TARIFFS_CARGO)
  .then(process.decodeResponseData(t.array(Tariff)))
);

export const useAutoGuide = (transportType: string): APIQueryResult<AutoGuide[], Error> => useAPI(['autoGuide', transportType],
  ({ http, process: { decodeResponseData } }) => http.get<AutoGuide[]>(`${GET_AUTO_GUIDE}/${transportType}`)
    .then(decodeResponseData(t.array(AutoGuide)))
);

export const useFilteredTariffs = (
  params: TariffFilter,
  config?: QueryConfig<TariffsInfo>
): APIQueryResult<TariffsInfo> => useAPI(
  ['filteredTariffsCargo', params],
  ({ http, process }) => http
    .post<TariffsInfo>(
      `${GET_ALL_TARIFFS_CARGO}${SEARCH}?size=${params.page.pageSize}&page=${params.page.pageNumber}`,
      {
        ...params,
      }
    )
    .then(process.decodeResponseData(TariffsInfo)),
  config
);

// todo разобраться с типами
export const useGetPackData = (): APIQueryResult<PackData> => useAPI(['pack'], ({ http, process }) => http.get<PackData>(GET_PACK_CARGO)
  .then(process.decodeResponseData()));

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useGetPackTariff = (contractorId: string | null | undefined): APIQueryResult<any, AxiosError> => {
  return useAPI(['tariffPack', contractorId], ({ http, process }) => contractorId
    ? http
      .get<TariffPack>(GET_TARIFF_PACK, { urlParams: { packId: contractorId as string } })
      .then(process.decodeResponseData())
    : []
  );
};

export const usePostPackTariff = () => useAPIMutation(
  ({ http, process }, pack: CargoPackPost) => http
    .post<CargoPackPost>(GET_ALL_TARIFFS_PACK, pack)
    .then(process.getResponseData)
    .catch(() => null),
  {
    onSuccess: ({
      cache, result: tariff, process,
    }) => {
      if (tariff !== null) {
        process.processStatus(200, 'Тариф создан');
        cache.refetchQueries(['tariffsPack']);
        cache.refetchQueries(['tariffPack']);
      }
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.crudMessages.saveFailed);
    },
  }
);

export const usePutPackTariff = () => useAPIMutation(
  ({ http, process }, pack: CargoPackPut) => http
    .put(TARIFF_PACK_BY_ID, pack, { urlParams: { packId: pack.id } })
    .then(process.getResponseData)
    .catch(() => null),
  {
    onSuccess: ({
      cache, result: tariff, process,
    }) => {
      if (tariff !== null) {
        process.processStatus(200, 'Тариф создан');
        cache.refetchQueries(['tariffsPack']);
        cache.refetchQueries(['tariffPack']);
      }
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.crudMessages.saveFailed);
    },
  }
);

export const useCreateTariff = () => useAPIMutation(
  ({ http, process }, { tariff, transTypeId }: { tariff: TariffJson; transTypeId: string }) => http
    .post(CREATE_TARIFF_CARGO, TariffJson.encode(tariff), { urlParams: { transTypeId } })
    // @ts-ignore
    .then<TariffJson>(process.getResponseData),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Tariffs.tariffAddSuccess);
      cache.refetchQueries(['tariffsCargo']);
      cache.refetchQueries(['filteredTariffsCargo']);
    },
    onError: ({
      error, t, logger,
    }) => errorHandler(error, t, logger),
  }
);

export const useUpdateTariff = () => useAPIMutation(
  (
    { http },
    {
      tariff, transTypeId, tariffId,
    }: { tariff: TariffJson; transTypeId: string; tariffId: string }
  ) => http.put(GET_TARIFF_CARGO, TariffJson.encode(tariff), { urlParams: { transTypeId, tariffId } }),
  {
    onSuccess: ({
      cache, t, process, variables,
    }) => {
      process.processStatus(200, t.Tariffs.tariffEditSuccess);
      cache.refetchQueries(['tariffsCargo']);
      cache.refetchQueries(['filteredTariffsCargo']);
      cache.refetchQueries(['tariffCargo', variables.tariffId]);
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
  ['tariffCargo', tariffId],
  ({ http, process }) => http
    .get<TariffJson>(GET_TARIFF_CARGO, { urlParams: { transTypeId, tariffId } })
    .then(process.decodeResponseData(TariffJson)),
  config
);

export const useDeleteTariff = (): MutationResultPair<
  AxiosResponse<number>,
  unknown,
  { transTypeId: string; tariffId: UUID },
  unknown
> => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http }, { transTypeId, tariffId }: { transTypeId: string; tariffId: UUID }) => http.delete<number>(DELETE_TARIFF_CARGO, { urlParams: { transTypeId, tariffId } }),
  {
    onSuccess: ({
      cache, process, t, variables,
    }) => {
      process.processStatus(200, t.Tariffs.tariffDeleteSuccess);
      cache.refetchQueries(['tariffsCargo']);
      cache.refetchQueries(['filteredTariffsCargo']);
      cache.refetchQueries(['tariffCargo', variables.tariffId]);
    },
    onError: ({ t, logger }) => {
      logger.toMessage('error', t.Tariffs.InternalServerError);
    },
  }
);

export const useGetListRegions = (): APIQueryResult<RegionInfoType[], Error> => useAPI(['RegionInfoTypeCargo'], ({ http, process: { decodeResponseData } }) => http.get<RegionInfoType[]>(`/${GET_ALL_GEO_ZONES}`)
  .then(decodeResponseData(t.array(RegionInfoType)))
);

export const useUploadTariffs = (entity: keyof typeof importExportEndpointMap) => mkUseUploadEntity(entity, {
  onSuccess: ({ cache }) => {
    cache.invalidateQueries(['tariffsCargo']);
    cache.invalidateQueries(['filteredTariffsCargo']);
    cache.refetchQueries(['import-report', entity]);
  },
});

export const useUploadCargoTariffs = (entity: keyof typeof importExportEndpointMap) => mkUseUploadCargoEntity(entity, {
  onSuccess: ({ cache }) => {
    cache.invalidateQueries(['tariffsCargo']);
    cache.invalidateQueries(['filteredTariffsCargo']);
    cache.refetchQueries(['import-report', entity]);
  },
});

export const useTransportTypeTariff = (
  tariffId: UUID | null | undefined,
  transportType: keyof typeof TransportTypesCargo
): APIQueryResult<TariffJson | null> => useAPI(['transportTypeTariffCargo', tariffId, transportType], ({ http, process }) => tariffId
  ? {
    DEDICATED: () => http.get<TariffJson>(GET_DEDICATED_TARIFF_CARGO, { urlParams: { tariffId } }),
    COURIER: () => http.get<TariffJson>(GET_COURIER_TARIFF_CARGO, { urlParams: { tariffId } }),
    INTERREGIONAL: () => http.get<TariffJson>(GET_INTERREGIONAL_TARIFF_CARGO, { urlParams: { tariffId } }),
    DOMESTIC_COURIER: () => http.get<TariffJson>(GET_DOMESTIC_COURIER_TARIFF_CARGO, { urlParams: { tariffId } }),
    INDIVIDUAL: () => http.get<TariffJson>(GET_INDIVIDUAL_TARIFF_CARGO, { urlParams: { tariffId } }),
  }[transportType]().then(process.decodeResponseData(TariffJson))
  : null
);
