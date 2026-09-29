import type { ResponseService, IHttpService } from '@sber-sbertransport/mf-core';
import {
  CALCULATE_TRIP_COST,
  CANCEL_REQUEST_PARAMS,
  DELETE_REQUEST_PARAMS,
  EDIT_REQUEST_PARAMS,
  FINISH_REQUEST_PARAMS,
  GET_ALL_PURPOSES,
  GET_ALL_TARIFFS_PERSONAL,
  GET_ALL_TARIFFS_TAXI,
  GET_REQUEST_PARAMS,
  RATE_REQUEST_PARAMS,
  REQUEST_APPROVE_PARAMS,
  REQUEST_DECLINE_PARAMS,
  SAVE_REQUEST,
  SEARCH_REQUESTS
} from 'constants/constants.api';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import * as t from 'io-ts';
import {
  DeclineReason,
  IOTariffPersonal,
  IOTariffTaxi,
  RequestRating,
  ITripCalculateRequest,
  ITripPrice,
  ITripService,
  Purpose,
  TTariffPersonal,
  TTariffTaxi,
  TripCancelReason,
  TripRequest
} from './Trip.interface';

@injectable()
export class DITripService implements ITripService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: ResponseService;

  calculateTripCost(data: ITripCalculateRequest): Promise<ITripPrice[]> {
    // @ts-ignore
    return this.http.post(`${CALCULATE_TRIP_COST}`, data).then<ITripPrice[]>(this.process.getResponseData);
  }

  saveTripRequest(data: TripRequest): Promise<TripRequest> {
    // @ts-ignore
    return this.http.post(`${SAVE_REQUEST}`, data).then<TripRequest>(this.process.getResponseData);
  }

  editTripRequest(data: TripRequest, requestId: string): Promise<number> {
    return this.http
      .put(`${EDIT_REQUEST_PARAMS}`, data, { urlParams: { requestId } })
      .then(this.process.getResponseStatus);
  }

  rateTripRequest(data: RequestRating, requestId: string): Promise<number> {
    return this.http
      .post(`${RATE_REQUEST_PARAMS}`, data, { urlParams: { requestId } })
      .then(this.process.getResponseStatus);
  }

  getTripRequestList(authorId: string): Promise<TripRequest[]> {
    return this.http
      .get<TripRequest[]>(`${SEARCH_REQUESTS}`, { params: { authorId } })
      .then(this.process.decodeResponseData(t.array(TripRequest)));
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getApprovementList(searchParams: Record<string, any>): Promise<TripRequest[]> {
    return this.http
      .get(`${SEARCH_REQUESTS}`, { params: { ...searchParams } })
      // @ts-ignore
      .then<TripRequest[]>(this.process.getResponseData);
  }

  getTripRequest(requestId: string): Promise<TripRequest> {
    return this.http
      .get(`${GET_REQUEST_PARAMS}`, { urlParams: { requestId } })
      // @ts-ignore
      .then<TripRequest>(this.process.getResponseData);
  }

  getAllPurposes(): Promise<Purpose[]> {
    // @ts-ignore
    return this.http.get(`${GET_ALL_PURPOSES}`).then<Purpose[]>(this.process.getResponseData);
  }

  getAllTariffsTaxi(): Promise<TTariffTaxi[]> {
    return this.http
      .get<TTariffTaxi[]>(`${GET_ALL_TARIFFS_TAXI}`)
      .then(this.process.decodeResponseData(t.array(IOTariffTaxi)));
  }

  getAllTariffsPersonal(): Promise<TTariffPersonal[]> {
    return this.http
      .get<TTariffPersonal[]>(`${GET_ALL_TARIFFS_PERSONAL}`)
      .then(this.process.decodeResponseData(t.array(IOTariffPersonal)));
  }

  deleteRequest(requestId: string): Promise<number> {
    return this.http
      .delete(`${DELETE_REQUEST_PARAMS}`, { urlParams: { requestId } })
      .then(this.process.getResponseStatus);
  }

  cancelRequest(requestId: string, reason: TripCancelReason): Promise<number> {
    return this.http
      .put(`${CANCEL_REQUEST_PARAMS}`, reason, { urlParams: { requestId } })
      .then(this.process.getResponseStatus);
  }

  approveTripRequest(reqId: string): Promise<TripRequest> {
    return this.http
      .post(`${REQUEST_APPROVE_PARAMS}`, {}, { urlParams: { reqId } })
      // @ts-ignore
      .then<TripRequest>(this.process.getResponseData);
  }

  declineTripRequest(reqId: string, data: DeclineReason): Promise<number> {
    return this.http
      .put<number>(`${REQUEST_DECLINE_PARAMS}`, data, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }

  finishTripRequest(reqId: string): Promise<number> {
    return this.http
      .post(`${FINISH_REQUEST_PARAMS}`, {}, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }
}
