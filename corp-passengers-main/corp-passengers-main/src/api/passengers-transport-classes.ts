import {
  GET_GROUP_TRANSFER_CLASSES,
  GET_TAXI_CLASSES
} from 'constants/constants.api';
import { TransportClass } from 'stores/TransportClasses/TransportClasses.interface';
import * as t from 'io-ts';
import { APIQueryResult, useAPI } from 'api';
import { QueryConfig } from 'react-query';

declare module 'api' {
  interface Cache {
    taxiTransportClasses: { key: ['taxiTransportClasses']; value: TransportClass[] };
    groupTransferTransportClasses: { key: ['groupTransferTransportClasses']; value: TransportClass[] };
  }
}

export const useTaxiTransportClasses = (
  config?: QueryConfig<TransportClass[], Error>
): APIQueryResult<TransportClass[], Error> => useAPI(
  ['taxiTransportClasses'],
  ({ http, process: { decodeResponseData } }) => (
    http.get<TransportClass[]>(GET_TAXI_CLASSES).then(decodeResponseData(t.array(TransportClass)))
  ),
  config
);

export const useGroupTransferTransportClasses = (
  config?: QueryConfig<TransportClass[], Error>
): APIQueryResult<TransportClass[], Error> => useAPI(
  ['groupTransferTransportClasses'],
  ({ http, process: { decodeResponseData } }) => (
    http.get<TransportClass[]>(GET_GROUP_TRANSFER_CLASSES).then(decodeResponseData(t.array(TransportClass)))
  ),
  config
);
