import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';

import {
  GET_ALL_REPORTS,
  GET_BICYCLE_TARIFF,
  GET_CARSHARING_TARIFF,
  GET_COURIER_TARIFF,
  GET_DEDICATED_TARIFF,
  GET_DOMESTIC_COURIER_TARIFF,
  GET_INTERREGIONAL_TARIFF,
  GET_PERSONAL_TARIFF,
  GET_PUBLIC_TARIFF,
  GET_SCOOTER_TARIFF,
  GET_TAXI_TARIFF
} from '../constants/constants.api';
import {
  TariffsSearchResponse, RegionInfoType, Tariff, TariffFilter, TariffJson, TariffsInfo
} from 'stores/Tariffs/Tariffs.interface';
import { TransportTypes } from '../stores/TransportTypes/TransportTypes.interface';
import { UUID } from '../utils/io-ts';

enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
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

export const useGetListRegions = (
  config?: QueryConfig<RegionInfoType[], Error>
): APIQueryResult<RegionInfoType[], Error> => useAPI(
  ['RegionInfoType'],
  ({ http, process: { decodeResponseData } }) => http.get<RegionInfoType[]>(`/geo-zones/`).then(decodeResponseData(t.array(RegionInfoType))),
  config
);

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
