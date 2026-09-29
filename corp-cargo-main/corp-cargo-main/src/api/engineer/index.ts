import { Method, Service } from 'stores/Roles/Roles.interface';
import {
  CANCEL_ORDER,
  CARGO_HOME_CLICK_REQUEST_COMPLETE_TRANSFER,
  CARGO_REQUEST_COMPLETE_SHIPMENT,
  CARGO_REQUEST_COMPLETE_TRANSFER,
  CARGO_REQUEST_HOME_CLICK_COMPLETE_SHIPMENT,
  CLEAR_QUERY_CONFIG,
  GET_CARGO_REQUEST_STATUS,
  REQUESTS_OTO_FEED_CARGO_BY_ORG_ID,
  REQUESTS_OTO_FEED_PERSON_BY_ORG_ID,
  UPDATE_CARGO_HOME_CLICK_STATUS,
  UPDATE_CARGO_REQUEST_STATUS
} from 'constants/constants.api';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { Feed, RequestBodyParamsOTO } from 'stores/Engineer/Models/Feed';
import qs from 'qs';
import { Fields } from 'modules/Engineers/components/Search';
import { FieldsCargo } from 'modules/Engineers/components/Search/CargoSearch';
import { getMaskPhone } from 'utils/getMaskPhone';
import { formatAddressForRequest } from 'utils/formatAddress';
import { toRequestDateRange } from 'shared/components/DateInput/utils';
import {
  clearSymbols, isEmptyObject, isFilledObject, stripEmpty, getErrorMessage
} from '../../utils';
import moment from 'moment';
import { SortDirection, SortFields, SortSetting } from 'stores/Engineer/Models/Settings/SortSettings';
import { FeedCargo, RequestCargoBodyParamsOTO } from 'stores/Engineer/Models/FeedCargo';
import * as t from 'io-ts';
import { MutationResultPair } from 'react-query';
import { FeedContent } from 'stores/Engineer/Models/Feed/Feed.content';
import { ADDRESS_TYPE } from 'stores/Engineer/Models';
import { UUID } from 'utils/io-ts';
import { TripStatus, useGettingAllTravelStatuses } from '../travel-status';
import { AxiosError } from 'axios';

enum Source {
  WEB = 'WEB',
  HOME_CLICK = 'HOME_CLICK',
  WEB_MULTIPLE = 'WEB_MULTIPLE',
}

declare module 'api' {
  interface Cache {
    feed: { key: ['feed', RequestBodyParamsOTO | undefined]; value: Feed };
    feedCargo: { key: ['feedCargo', RequestCargoBodyParamsOTO | undefined]; value: FeedCargo };
    feedWithId: { key: ['feedWithId']; value: Feed };
    service: { key: ['services', string | null]; value: Service[] };
    methods: { key: ['methods', string, string | null]; value: Method[] };
    cargoStatus: { key: ['cargoStatus']; value: TripStatus[] };
    cargoCancel: { key: ['cargoCancel']; value: string };
  }
}

export const useOtoFeed = (orgId: UUID, query?: RequestBodyParamsOTO): APIQueryResult<Feed, Error> => useAPI(
  ['feed', query],
  ({ http, process }) => http
    .get<Feed>(REQUESTS_OTO_FEED_PERSON_BY_ORG_ID, {
      urlParams: { organizationId: orgId },
      params: { ...query },
      paramsSerializer: params => qs.stringify(params, { arrayFormat: 'brackets' }),
    })
    .then(process.decodeResponseData(Feed)),
  { refetchOnMount: false, cacheTime: 10 * 1000 }
);

// eslint-disable-next-line @stylistic/max-len
export const useOtoFeedCargo = (orgId: UUID, query?: RequestCargoBodyParamsOTO): APIQueryResult<FeedCargo, Error> => useAPI(
  ['feedCargo', query],
  ({ http, process }) => http
    .get<Feed>(REQUESTS_OTO_FEED_CARGO_BY_ORG_ID, {
      urlParams: { organizationId: orgId },
      params: { ...query },
      paramsSerializer: params => qs.stringify(params, { arrayFormat: 'brackets' }),
    })
    .then(process.decodeResponseData(FeedCargo)),
  { refetchOnMount: false, cacheTime: 10 * 1000 }
);

export const useCargoStatuses = (): APIQueryResult<TripStatus[], Error> => useAPI(['cargoStatus'], ({ http, process: { decodeResponseData } }) => http.get<TripStatus[]>(GET_CARGO_REQUEST_STATUS).then(decodeResponseData(t.array(TripStatus)))
);

export const useCarsharingStatuses = (): APIQueryResult<TripStatus[], Error> => {
  // filter all statuses as backend is not supporting carsharing statuses request
  const resp = useGettingAllTravelStatuses();
  resp.data = resp.data.filter(item => item.name.startsWith('CARSHARING_'));
  return resp;
};

export const useChangeCargoRequestStatus = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; status: string; source?: Source },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, status, source = 'WEB',
  }) => {
    const REQUEST_LINK = source === 'HOME_CLICK' ? UPDATE_CARGO_HOME_CLICK_STATUS : UPDATE_CARGO_REQUEST_STATUS;
    return http
      .post<FeedContent>(REQUEST_LINK, {}, { urlParams: { requestId, status } })
      .then(process.getResponseData);
  },
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, process, t: tf,
    }) => {
      process.processStatus(200, tf.DetailedView.StatusEditMessages.isEditedSuccessfully);
      cache.refetchQueries(['feedCargo'], { exact: true, active: true });
    },
    onError: ({ process, t: tf }) => {
      process.processStatus(409, tf.DetailedView.StatusEditMessages.isEditedWithErrors);
    },
  }
);

export const useChangeCargoTransferTime = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; transferTime: string; status: string; source?: Source },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, transferTime, source = 'WEB',
  }) => {
    const REQUEST_LINK = source === 'HOME_CLICK' ? CARGO_HOME_CLICK_REQUEST_COMPLETE_TRANSFER : CARGO_REQUEST_COMPLETE_TRANSFER;
    return http
      .patch<FeedContent>(REQUEST_LINK, [{ field: 'TRANSFER_DATE', value: transferTime }], {
        urlParams: {
          requestId,
        },
      })
      .then(process.getResponseData);
  },
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, process, t: tf,
    }) => {
      process.processStatus(200, tf.DateEditMessages.isEditedSuccessfully);
      cache.refetchQueries(['feedCargo'], { exact: true, active: true });
    },
    onError: ({ process, t: tf }) => {
      process.processStatus(409, tf.DateEditMessages.isEditedWithErrors);
    },
  }
);

export const useChangeCargoShipmentTime = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; shipmentTime: string; status: string; source?: Source },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, shipmentTime, source = 'WEB',
  }) => {
    const REQUEST_LINK = source === 'HOME_CLICK' ? CARGO_REQUEST_HOME_CLICK_COMPLETE_SHIPMENT : CARGO_REQUEST_COMPLETE_SHIPMENT;
    return http
      .patch<FeedContent>(REQUEST_LINK, [{ field: 'SHIPMENT_DATE', value: shipmentTime }], {
        urlParams: {
          requestId,
        },
      })
      .then(process.getResponseData);
  },
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, process, t: tf,
    }) => {
      process.processStatus(200, tf.DateEditMessages.isEditedSuccessfully);
      cache.refetchQueries(['feedCargo'], { exact: true, active: true });
    },
    onError: ({ process, t: tf }) => {
      process.processStatus(409, tf.DateEditMessages.isEditedWithErrors);
    },
  }
);

export const useSearchParams = (query: Fields): RequestBodyParamsOTO => {
  const timeMonthAgo = moment().add(-1, 'M').startOf('h').toISOString();
  let requestBody = {} as RequestBodyParamsOTO;

  const sessionStorageSortSettings = sessionStorage.getItem('engineerSortSetting');
  const sortSettings: SortSetting | null = sessionStorageSortSettings ? JSON.parse(sessionStorageSortSettings) : null;
  const savedSortField = sortSettings ? sortSettings.sortField : SortFields.deadline;
  const savedSortDirection = sortSettings && sortSettings.sortDirection ? SortDirection.ASC : SortDirection.DESC;

  if (!query || isEmptyObject(stripEmpty(query))) {
    return {
      pageSize: 50,
      page: 0,
      sortField: savedSortField ?? SortFields.deadline,
      sortDirection: savedSortDirection ?? SortDirection.DESC,
      creationTimeFrom: timeMonthAgo,
    };
  }

  const {
    page, pageSize, sortField, sortDirection, ...params
  } = query;

  if (isEmptyObject(stripEmpty(params))) {
    return {
      page: page ?? 0,
      pageSize: pageSize ?? 50,
      sortField: sortField ?? savedSortField,
      sortDirection: sortDirection ?? savedSortDirection,
      creationTimeFrom: timeMonthAgo,
    };
  }

  if (isFilledObject(stripEmpty(params))) {
    const {
      id,
      status,
      transportType,
      contractor,
      addressType,
      addressValue,
      passengerDepartment,
      passengerPhone,
      passengerPosition,
      dateTimeType,
      date,
      deadline,
      passengerName,
      transportClass,
    } = params;

    const {
      creationTimeFrom,
      creationTimeTo,
      desiredTimeFrom,
      desiredTimeTo,
      startTimeFrom,
      startTimeTo,
      finishTimeFrom,
      finishTimeTo,
      deadlineFrom,
      deadlineTo,
    } = toRequestDateRange(dateTimeType, date, deadline, false, true);

    requestBody = {
      id: id && clearSymbols(id).trim(),
      status: status ? status.split(',') : undefined,
      transportType,
      contractor,
      passengerName: passengerName && clearSymbols(passengerName).trim(),
      passengerPhone: passengerPhone && getMaskPhone(passengerPhone),
      transportClass,
      passengerPosition,
      passengerDepartment,
      creationTimeFrom,
      creationTimeTo,
      desiredTimeFrom,
      desiredTimeTo,
      startTimeFrom,
      startTimeTo,
      finishTimeFrom,
      finishTimeTo,
      deadlineFrom,
      deadlineTo,
      page: page ?? 0,
      pageSize: pageSize ?? 50,
      sortField: sortField ?? savedSortField,
      sortDirection: sortDirection ?? savedSortDirection,
    };

    if (addressType === 'DEPARTURE_ADDRESS') {
      requestBody.departure = formatAddressForRequest(addressValue);
    } else if (addressType === 'DESTINATION_ADDRESS') {
      requestBody.destination = formatAddressForRequest(addressValue);
    } else if (addressType === 'WAYPOINT_ADDRESS') {
      requestBody.waypoint = formatAddressForRequest(addressValue);
    }
  }
  return requestBody;
};

export const useCancelOrder = (): MutationResultPair<
  unknown,
  unknown,
  { requestId: string; reason: string; code: number; field: string; value: string },
  unknown
  > => useAPIMutation(
  ({ http, process }, {
    requestId, reason, code, value, field,
  }) => (
    http
      .patch<{ requestId: string; reason: string; code: number; field: string; value: string }>(
        CANCEL_ORDER,
        [{
          reason,
          code,
          field,
          value,
        }],
        { urlParams: { requestId } })
      .then(process.getResponseData)
  ),
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({ process, t: tf }) => {
      process.processStatus(200, tf.Monitor.isCancelOrder);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error as AxiosError));
    },
  }
);

export const useCargoSearchParams = (query: FieldsCargo): RequestCargoBodyParamsOTO => {
  const timeMonthAgo = moment().add(-1, 'M').startOf('h').toISOString();
  let requestBody = {} as RequestCargoBodyParamsOTO;

  const sessionStorageSortSettings = sessionStorage.getItem('engineerSortSetting');
  const sortSettings: SortSetting | null = sessionStorageSortSettings ? JSON.parse(sessionStorageSortSettings) : null;
  const savedSortField = sortSettings ? sortSettings.sortField : SortFields.creationTime;
  const savedSortDirection = sortSettings && sortSettings.sortDirection ? SortDirection.ASC : SortDirection.DESC;

  if (!query || isEmptyObject(stripEmpty(query))) {
    return {
      pageSize: 50,
      page: 0,
      sortField: savedSortField ?? SortFields.creationTime,
      sortDirection: savedSortDirection ?? SortDirection.DESC,
      creationTimeFrom: timeMonthAgo,
    };
  }

  const {
    page, pageSize, sortField, sortDirection, ...params
  } = query;

  if (isEmptyObject(stripEmpty(params))) {
    return {
      page: page ?? 0,
      pageSize: pageSize ?? 50,
      sortField: sortField ?? savedSortField,
      sortDirection: sortDirection ?? savedSortDirection,
      creationTimeFrom: timeMonthAgo,
    };
  }

  if (isFilledObject(stripEmpty(params))) {
    const {
      id, status, addressType, addressValue, dateTimeType, date, senderName, recipientName,
    } = params;

    const {
      creationTimeTo,
      creationTimeFrom,
      desiredTimeTo,
      desiredTimeFrom,
      approvalTimeTo,
      approvalTimeFrom,
      transferTimeTo,
      transferTimeFrom,
      shipmentTimeTo,
      shipmentTimeFrom,
    } = toRequestDateRange(dateTimeType, date, undefined, false, true);

    requestBody = {
      id: id && clearSymbols(id).trim(),
      status: status ? status.split(',') : undefined,
      senderName,
      recipientName,
      creationTimeFrom,
      creationTimeTo,
      desiredTimeFrom,
      desiredTimeTo,
      approvalTimeTo,
      approvalTimeFrom,
      transferTimeTo,
      transferTimeFrom,
      shipmentTimeTo,
      shipmentTimeFrom,
      page: page ?? 0,
      pageSize: pageSize ?? 50,
      sortField: sortField ?? savedSortField,
      sortDirection: sortDirection ?? savedSortDirection,
    };

    if (addressType === ADDRESS_TYPE.DEPARTURE_ADDRESS.toString()) {
      requestBody.senderAddress = formatAddressForRequest(addressValue);
    } else if (addressType === ADDRESS_TYPE.RECIPIENT_ADDRESS.toString()) {
      requestBody.recipientAddress = formatAddressForRequest(addressValue);
    }
  }
  return requestBody;
};
