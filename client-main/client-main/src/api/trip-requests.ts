import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';

import { SYSTEM_MESSAGES } from 'constants/constants.app';
import {
  CALCULATE_TARIFFS_COST,
  CANCEL_REQUEST_PARAMS,
  CHANGE_REQUEST_STATUS,
  COMPLETE_TRIP_REQUEST,
  CREATE_PUBLIC_TRIP_COMPENSATION,
  EDIT_REQUEST_PARAMS,
  GET_APPROVAL_PUBLIC,
  GET_PUBLIC_COMPENSATION_TYPES,
  GET_PUBLIC_TRANSPORT_TYPES,
  GET_REQUEST_STATUSES,
  GET_REQUEST_TRIP_INFO,
  GET_TRIP_FROM_COOP,
  SEARCH_REQUESTS,
  UPDATE_APPROVED_TRIP_REQUEST
} from 'constants/constants.env';

import { TTripRequestStatuses } from 'constants/TripRequestStatuses.constants';

import { TripRequestModel } from 'stores/Trip/models';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import {
  IOTripFromCoop,
  ITripCalculateRequest,
  ITripTariff,
  PublicTripCompensation,
  TTripFromCoop,
  TripCancelReason,
  TripRequest
} from 'stores/Trip/Trip.interface';
import { plainToNew } from 'utils';

import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { ActualRoute } from '../shared/hooks/trip/useTripRequestRoute';

import { RequestDetailsResponse } from '../stores/Request/Response/types';
import { TransportTypeEnum } from '../stores/TransportTypes/TransportTypes.interface';
import { ioTypeFromEnum } from '../utils/ioTypeFromEnum';

export interface RequestStatus {
  name: string;
  rusName: string;
}

export const RequestStatus = t.type({
  name: t.string,
  rusName: t.string,
});

export interface AvailablePublicTransportType {
  name: string;
  rusName: string;
  publicCompensationType: string;
}

export const AvailablePublicTransportType = t.type({
  name: t.string,
  rusName: t.string,
  publicCompensationType: t.string,
});

export interface AvailablePublicTTCompensationType {
  name: string;
  rusName: string;
  attachmentDocumentRequired: boolean;
  expirationDatesRequired: boolean;
}

export const AvailablePublicTTCompensationType = t.type({
  name: t.string,
  rusName: t.string,
  attachmentDocumentRequired: t.boolean,
  expirationDatesRequired: t.boolean,
});

export const TripPurpose = t.type({
  id: tt.uuid,
  label: t.string,
});

export const Region = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    name: t.string,
    code: t.number,
    parentId: tt.uuid,
  }),
]);

export const PurposeAndRegionItem = t.partial({
  tripPurpose: TripPurpose,
  region: Region,
  minCostToBeApproved: t.number,
});

export const PublicApprovals = t.partial({
  organizationId: tt.uuid,
  id: tt.uuid,
  approvalActive: t.boolean,
  minCostToBeApproved: t.number,
  transportType: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
  purposeAndRegionItems: t.array(PurposeAndRegionItem),
  approvalDocumentCheck: t.boolean,
  affirmativeActive: t.boolean,
  tripConfirmationActive: t.boolean,
  tripConfirmationDocumentCheck: t.boolean,
});

export type PublicApprovals = t.TypeOf<typeof PublicApprovals>;

declare module 'api' {
  interface Cache {
    tripRequestInfo: {
      key: ['tripRequestInfo', UUID];
      value: TripRequestModel;
    };
    constantsTaxiStatuses: {
      key: ['constantsTaxiStatuses'];
      value: RequestStatus[];
    };
    constantsPersonalStatuses: {
      key: ['constantsPersonalStatuses'];
      value: RequestStatus[];
    };
    constantsCarsharingStatuses: {
      key: ['constantsCarsharingStatuses'];
      value: RequestStatus[];
    };
    constantsPublicStatuses: {
      key: ['constantsPublicStatuses'];
      value: RequestStatus[];
    };
    tripFromCoop: {
      key: ['tripFromCoop', UUID];
      value: TTripFromCoop;
    };
    tariffCosts: {
      key: ['tariffCosts', ITripCalculateRequest];
      value: TripPriceModel[];
    };
    availableTransportTypes: {
      key: ['availableTransportTypes'];
      value: AvailablePublicTransportType[];
    };
    availableCompensationPublicTransportTypes: {
      key: ['availableCompensationPublicTransportTypes'];
      value: AvailablePublicTTCompensationType[];
    };
    publicTripCompensation: {
      key: ['publicTripCompensation'];
    };
    tripParticipants: {
      key: ['participantsTrips', UUID];
      value: TripRequest[];
    };
    publicApprovals: {
      key: ['publicApprovals', string | undefined];
      value: PublicApprovals | null;
    };
    requestDetailsResponse: {
      key: ['requestDetailsResponse', string];
      value: RequestDetailsResponse | null;
    };
  }
}

export const useGetRequestTripInfoById = (
  reqId: UUID,
  options: QueryConfig<TripRequestModel, unknown>
): APIQueryResult<TripRequestModel, unknown> => useAPI(
  ['tripRequestInfo', reqId],
  ({ http, process }) => http
    .get<TripRequest>(GET_REQUEST_TRIP_INFO, { urlParams: { reqId } })
    .then(process.decodeResponseData(TripRequest))
    .then(approvals => plainToNew<TripRequestModel>(TripRequestModel, approvals)),
  options
);

export const useGetRequestTaxiStatusesList = (): APIQueryResult<RequestStatus[], unknown> => useAPI(['constantsTaxiStatuses'], ({ http, process }) => http.get<RequestStatus[]>(`${GET_REQUEST_STATUSES}/taxi`).then(process.decodeResponseData(t.array(RequestStatus)))
);

export const useGetAvailablePublicTransportTypes = (): APIQueryResult<AvailablePublicTransportType[], unknown> => useAPI(['availableTransportTypes'], ({ http, process }) => http
  .get<AvailablePublicTransportType[]>(GET_PUBLIC_TRANSPORT_TYPES)
  .then(process.decodeResponseData(t.array(AvailablePublicTransportType)))
);

export const useGetAvailablePublicTTCompensations = (): APIQueryResult<AvailablePublicTTCompensationType[], unknown> => useAPI(['availableCompensationPublicTransportTypes'], ({ http, process }) => http
  .get<AvailablePublicTTCompensationType[]>(GET_PUBLIC_COMPENSATION_TYPES)
  .then(process.decodeResponseData(t.array(AvailablePublicTTCompensationType)))
);

export const useCreatePublicTripCompensationRequest = (
  data: PublicTripCompensation
): MutationResultPair<number | Error, unknown, PublicTripCompensation[], unknown> => useAPIMutation(
  ({ http, process }): Promise<any> => http
    .post<PublicTripCompensation>(CREATE_PUBLIC_TRIP_COMPENSATION, { ...data })
    .then(x => process.getResponseStatus(x))
    .catch(y => process.getResponseStatus(y)),
  {
    onSuccess: ({ cache }) => {
      // eslint-disable-next-line no-console
      console.log('cache', cache);
    },
  }
);

export const useGetRequestPersonalStatusesList = (): APIQueryResult<RequestStatus[], unknown> => useAPI(['constantsPersonalStatuses'], ({ http, process }) => http
  .get<RequestStatus[]>(`${GET_REQUEST_STATUSES}/personal`)
  .then(process.decodeResponseData(t.array(RequestStatus)))
);

export const useGetRequestPublicStatusesList = (): APIQueryResult<RequestStatus[], unknown> => useAPI(['constantsPublicStatuses'], ({ http, process }) => http.get<RequestStatus[]>(`${GET_REQUEST_STATUSES}/public`).then(process.decodeResponseData(t.array(RequestStatus)))
);

export const useGetRequestCarsharingStatusesList = (): APIQueryResult<RequestStatus[], unknown> => useAPI(['constantsCarsharingStatuses'], ({ http, process }) => http
  .get<RequestStatus[]>(`${GET_REQUEST_STATUSES}/carsharing`)
  .then(process.decodeResponseData(t.array(RequestStatus)))
);

export const useChangeRequestStatus = (): MutationResultPair<
  unknown,
  unknown,
  { reqId: UUID; reqStatus: TTripRequestStatuses },
  unknown
> => useAPIMutation(
  ({ http, process }, { reqId, reqStatus }) => http
    .post(
      CHANGE_REQUEST_STATUS,
      {},
      {
        urlParams: { reqId, status: reqStatus as unknown as string },
      }
    )
    .then(process.getResponseData),
  // TODO add update query cache on success after move requests list from store to react-query realization
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Статус изменен');
    },
  }
);

export const useCompleteTripRequest = (): MutationResultPair<unknown, unknown, { reqId: string }, unknown> => useAPIMutation(
  ({ http, process }, { reqId }) => http.post(COMPLETE_TRIP_REQUEST, {}, { urlParams: { reqId } }).then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Поездка успешно завершена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при завершении поездки');
    },
  }
);

export const useCalculateTariffCost = (
  data: ITripCalculateRequest,
  options: QueryConfig<TripPriceModel[], unknown>
): APIQueryResult<TripPriceModel[], unknown> => useAPI(
  ['tariffCosts', data],
  ({ http, process }) => http
    .post<ITripTariff[]>(CALCULATE_TARIFFS_COST, { ...data })
    .then(process.getResponseData)
    .then(costs => plainToNew<TripPriceModel[]>(TripPriceModel, costs)),
  options
);

export const useEditTripRequest = (): MutationResultPair<
  unknown,
  any,
  { data: Partial<TripRequest>; reqId: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { data, reqId }) => http.put(EDIT_REQUEST_PARAMS, data, { urlParams: { reqId } }).then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Информация о заявке успешно обновлена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'При обновлении произошла ошибка');
    },
  }
);

export const useCancelTripRequest = (): MutationResultPair<
  unknown,
  any,
  { reason: TripCancelReason; reqId: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { reason, reqId }) => http.put(CANCEL_REQUEST_PARAMS, reason, { urlParams: { reqId } }).then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.tripRequestCancel);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.tripRequestCancelError);
    },
  }
);

export const useEditApprovedTripRequest = (): MutationResultPair<
  unknown,
  any,
  { data: ActualRoute; reqId: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { data, reqId }) => http.put(UPDATE_APPROVED_TRIP_REQUEST, data, { urlParams: { reqId } }).then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Информация о заявке успешно обновлена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'При обновлении произошла ошибка');
    },
  }
);

export const useGetTripFromCoop = (
  reqId: UUID,
  options: QueryConfig<TTripFromCoop, unknown>
): APIQueryResult<TTripFromCoop, unknown> => useAPI(
  ['tripFromCoop', reqId],
  ({ http, process }) => http
    .get<TTripFromCoop>(GET_TRIP_FROM_COOP, { urlParams: { reqId } })
    .then(process.decodeResponseData(IOTripFromCoop)),
  options
);

export const useGetTripsByEmployees = (passengerId: UUID): APIQueryResult<TripRequest[], unknown> => useAPI(['participantsTrips', passengerId], ({ http, process }) => http
  .get<TripRequest[]>(SEARCH_REQUESTS, { params: { passengerId } })
  .then(process.decodeResponseData(t.array(TripRequest)))
);

export const useGetApprovalsPublic = (orgId: string | undefined): APIQueryResult<PublicApprovals | null, unknown> => useAPI(['publicApprovals', orgId], ({ http, process }) => orgId
  ? http
    .get<PublicApprovals>(GET_APPROVAL_PUBLIC, { urlParams: { orgId } })
    .then(process.decodeResponseData(PublicApprovals))
  : null
);

export const useGetCurrentDriverPosition = (
  requestId: string
): APIQueryResult<RequestDetailsResponse | null, unknown> => useAPI(
  ['requestDetailsResponse', requestId],
  ({ http, process }) => http
    .get<any>(`/requests/dispatcher-room/${requestId}/dispatcher-details`, { urlParams: { requestId } })
    .then(process.decodeResponseData(RequestDetailsResponse)),
  {
    cacheTime: 0,
    refetchOnMount: true,
  }
);
