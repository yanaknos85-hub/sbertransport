import type { ResponseService, IHttpService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import { UUID } from 'utils/io-ts';
import { TYPES } from 'ioc/types';

import {
  REQUESTS_OTO_FEED_PERSON_BY_ORG_ID, REQUESTS_OTO_FEED_PERSON, GET_TRIP_REQUESTS, GET_SHARED_TRIP_REQUESTS
} from 'constants/constants.api';

import { FeedSearchQuery, SearchResponse } from '../../interfaces/Orders.types';
import { IPassengerService } from '../../interfaces/Passengers/Passenger.interface';
import { TTripFromCoop, TripResponse } from 'stores/Registry/Registry.interface';
import { BUS_CLASSES_OPTIONS, TAXI_CLASSES_OPTIONS } from 'modules/OrderExecution/components/Filter/Tabs/PassengerSearch/constans';
import { Category } from 'modules/OrderExecution/constants/Tabs';

@injectable()
export class DIPassengerService implements IPassengerService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: ResponseService;

  createBusParams = (query: FeedSearchQuery) => {
    const _query = { ...query };
    const transportType = _query?.transportType;

    switch (transportType) {
      case Category.bus.toUpperCase(): {
        _query.transportType = 'TAXI';

        if (!_query?.transportClass) {
          _query.transportClass = BUS_CLASSES_OPTIONS.map(v => v.value).join();
        }
        break;
      }
      case Category.taxi.toUpperCase(): {
        if (!_query?.transportClass) {
          _query.transportClass = TAXI_CLASSES_OPTIONS.map(v => v.value).join();
        }
        break;
      }
    }

    return _query;
  };

  getPersonOrderList = async (orgId: UUID, query: FeedSearchQuery): Promise<SearchResponse> => this.http
    .get<SearchResponse>(REQUESTS_OTO_FEED_PERSON_BY_ORG_ID, { urlParams: { organizationId: orgId }, params: query })
    .then(this.process.getResponseData);

  getPersonOrderListExec = async (query: FeedSearchQuery, execIds?: UUID[]): Promise<SearchResponse> => this.http
    .get<SearchResponse>(REQUESTS_OTO_FEED_PERSON, { params: { ...query, executorGroupId: execIds?.reduce((acc, item, i, arr) => acc + item + (arr.length > i + 1 ? ',' : ''), '') } })
    .then(this.process.getResponseData);

  getOrder = async (orderId: UUID, transportType: string): Promise<TripResponse> => this.http
    .get<TripResponse>(GET_TRIP_REQUESTS, { urlParams: { transportType, requestId: orderId } })
    .then(this.process.getResponseData);

  getSharedTrip = async (orderId: UUID): Promise<TTripFromCoop> => this.http
    .get<TTripFromCoop>(GET_SHARED_TRIP_REQUESTS, { urlParams: { requestId: orderId } })
    .then(this.process.getResponseData);
}
