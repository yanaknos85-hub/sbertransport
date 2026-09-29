import { MutationResultPair, QueryConfig } from 'react-query';
import { StateNumberSearchOne, StateNumberSearchOneFilters, TelemechanicTransport } from './telemechanicTransport.types';
import { useAPI, useAPIMutation } from 'api';
import { TELEMECHANIC_TRANSPORT } from './telemechanicTransport.constants';
import { AxiosError } from 'axios';
import { ignore } from 'utils/utils';

export const TELEMECHANIC_TRANSPORT_KEY = 'telemechanicTransport';

declare module 'api' {
  interface Cache {
    telemechanicTransport: {
      key: [typeof TELEMECHANIC_TRANSPORT_KEY, StateNumberSearchOneFilters];
      value: StateNumberSearchOne;
    };
  }
}

/** Запрос автомобиля внутреннего автопарка по госномеру */
export const useTelemechanicTransport = (
  filters: StateNumberSearchOneFilters,
  config?: QueryConfig<StateNumberSearchOne>
) => (
  useAPI(
    [TELEMECHANIC_TRANSPORT_KEY, filters],
    ({ http, process }) => (
      http
        .get<StateNumberSearchOne>(TELEMECHANIC_TRANSPORT)
        .then(process.decodeResponseData(StateNumberSearchOne))
    ),
    config
  )
);

export const useTelemechanicTransportMutation = (): MutationResultPair<
  TelemechanicTransport,
  AxiosError<Error>,
  StateNumberSearchOneFilters,
  unknown
> => (
  useAPIMutation(({ http, process }, params) => (
    http
      .get<TelemechanicTransport>(TELEMECHANIC_TRANSPORT, { params })
      .then(process.getResponseData)
  ), {
    onSuccess: ignore,
  })
);
