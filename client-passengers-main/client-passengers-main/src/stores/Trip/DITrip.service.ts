import {
  IContractorDispatcherTransport, IContractorDispatcherTransports, IOSearchNumberTrip, ISearchDispatcherTransport,
  YandexTripRequest,
  YandexTripResponse
} from './Trip.interface';
/* eslint-disable @typescript-eslint/no-explicit-any */
import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { AxiosError } from 'axios';
import { inject, injectable } from 'inversify';
import * as t from 'io-ts';

import {
  ACCEPT_SUITABLE_COOPERATIVE_TRIP,
  CALCULATE_PERSONAL_CARS_TRIP_COSTS,
  CALCULATE_TARIFFS_COST,
  CREATE_PUBLIC_TRIP_COMPENSATION,
  CREATE_YANDEX_EXTERNAL_TRIP,
  DELETE_REQUEST_PARAMS,
  EDIT_REQUEST_PARAMS,
  FINISH_REQUEST_PARAMS,
  GET_ALL_TARIFFS_PERSONAL,
  GET_ALL_TARIFFS_PUBLIC,
  GET_ALL_TARIFFS_TAXI,
  GET_CONTRACTOR_DISPATCHER_TRANSPORT,
  GET_CONTRACTORS,
  GET_EXTERNAL_PRICES,
  GET_PURPOSES_BY_EMPLOYEE,
  GET_REQUEST_PARAMS,
  GET_REQUEST_SELF,
  GET_REQUESTS_HISTORY,
  GET_SUITABLE_COOPERATIVE_TRIPS,
  GET_TARIFF,
  GET_TARIFF_CARSHARING,
  GET_USER_AVATART,
  ORGANIZATIONS,
  RATE_REQUEST_PARAMS,
  REQUESTS,
  SAVE_CITY_TRIP_COMPENSATION,
  SAVE_CONFIRM_CARD_FILE,
  SAVE_CONFIRM_SUBURB_FILE,
  SAVE_FILE,
  SAVE_REQUEST,
  SAVE_SUBURB_COMPENSATION,
  SAVE_TRAVEL_CARD_REQUEST,
  SEARCH_REQUESTS,
  SELF_NON_TERMINAL,
  SELF_TERMINAL,
  SHARED_RIDE_SETTINGS
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { TripPaginationModel } from 'stores/Trip/models/TripPagination.model';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { UUID } from '../../utils/io-ts';
import {
  CityTripCompensation,
  IContractors,
  InnerCityTransportRequestInfo,
  IOCoopTripsSettings,
  IOTariffPersonal,
  IOTariffPublic,
  IOTariffTaxi,
  IOTripCoop,
  IRequestRating,
  ITarrifCarsharing,
  ITripCalculateRequest,
  ITripHistory,
  ITripRequestData,
  ITripService,
  ITripTariff,
  PublicTripCompensation,
  SavedFileInfo,
  TCoopTripsSettings,
  TExternalPrices,
  TripPurpose,
  TripRequest,
  TTariffPersonal,
  TTariffPublic,
  TTariffTaxi,
  TTripCoop,
  TTripRequestNew
} from './Trip.interface';

@injectable()
export class DITripService implements ITripService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  calculateTripCost(
    data: ITripCalculateRequest,
    callback: (transportTypeId: string, tariffId: UUID) => Record<string, unknown>
  ): Promise<ITripTariff[]> {
    return this.http.post<ITripTariff[]>(`${CALCULATE_TARIFFS_COST}`, data).then(response => {
      const actualTariffs = response.data.filter(d => d.transportType.name === 'PUBLIC')[0];

      if (actualTariffs) {
        callback('public', actualTariffs.id as UUID);
      }

      return this.process.getResponseData(response);
    });
  }

  calculateExternalPrices(data: TTripRequestNew): Promise<TExternalPrices[]> {
    return this.http.post<TExternalPrices[]>(GET_EXTERNAL_PRICES, data).then(this.process.getResponseData);
  }

  createYandexTrip(data: YandexTripRequest): Promise<YandexTripResponse> {
    return this.http.post<YandexTripResponse>(CREATE_YANDEX_EXTERNAL_TRIP, data).then(this.process.getResponseData);
  }

  calculateCostForPersonalCarTrip(data: ITripCalculateRequest): Promise<ITripTariff[]> {
    return this.http
      .post<ITripTariff[]>(`${CALCULATE_PERSONAL_CARS_TRIP_COSTS}`, data)
      .then(this.process.getResponseData);
  }

  saveTripRequest(data: TripRequest): Promise<TripRequest | string> {
    return this.http
      .post<TripRequest>(`${SAVE_REQUEST}`, data)
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        if (error.response?.status === 409) {
          return error.response;
        }

        return error.response?.data.message || error.response?.data.error || error.message;
      });
  }

  editTripRequest(data: TripRequest, reqId: string): Promise<number> {
    return this.http.put(`${EDIT_REQUEST_PARAMS}`, data, { urlParams: { reqId } }).then(this.process.getResponseStatus);
  }

  rateTripRequest(data: IRequestRating, reqId: string): Promise<any> {
    return this.http.post(`${RATE_REQUEST_PARAMS}`, data, { urlParams: { reqId } }).then(this.process.getResponseData);
  }

  getTripRequestList(passengerId: string): Promise<TripRequest[]> {
    return this.http
      .get<TripRequest[]>(`${SEARCH_REQUESTS}`, { params: { passengerId } })
      .then(this.process.getResponseData);
  }

  getTripRequestListTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }> {
    return this.http
      .post<TripPaginationModel & { content: TripRequest[] }>(`${SELF_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getTripRequestListNonTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }> {
    return this.http
      .post<TripPaginationModel & { content: TripRequest[] }>(`${SELF_NON_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getApprovementList(searchParams: Record<string, any>): Promise<TripRequest[]> {
    return this.http
      .get<TripRequest[]>(`${SEARCH_REQUESTS}`, { params: { ...searchParams } })
      .then(this.process.getResponseData);
  }

  getTripRequestHistory(reqId: string): Promise<ITripHistory> {
    return this.http
      .get<ITripHistory>(`${GET_REQUESTS_HISTORY}`, { urlParams: { reqId } })
      .then(this.process.getResponseData);
  }

  getContractorDispatcherTransports(data: ITripCalculateRequest, searchParams: ISearchDispatcherTransport): Promise<IContractorDispatcherTransports> {
    return this.http
      .post<IContractorDispatcherTransports>(`${GET_CONTRACTOR_DISPATCHER_TRANSPORT}`, data, { params: { ...searchParams } })
      .then(this.process.getResponseData);
  }

  getContractorDispatcherTransport(transportId: string, data: ITripCalculateRequest, searchParams: ISearchDispatcherTransport): Promise<IContractorDispatcherTransport> {
    return this.http
      .post<IContractorDispatcherTransport>(`${GET_CONTRACTOR_DISPATCHER_TRANSPORT}/${transportId}`, data, { params: { ...searchParams } })
      .then(this.process.getResponseData);
  }

  getTripRequest(reqId: string): Promise<TripRequest> {
    return this.http
      .get<TripRequest>(`${GET_REQUEST_PARAMS}`, { urlParams: { reqId } })
      .then(this.process.getResponseData);
  }

  getTripRequestForTransport(reqId: string, transportType: TransportTypeEnum): Promise<TripRequest> {
    return this.http
      .get<TripRequest>(`${REQUESTS}/${transportType}/${reqId}`, { urlParams: { reqId } })
      .then(this.process.getResponseData);
  }

  getAllPurposesByEmployee(orgId: string): Promise<TripPurpose[]> {
    return this.http
      .get<TripPurpose[]>(`${GET_PURPOSES_BY_EMPLOYEE}`, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }

  getAllTariffsTaxi(): Promise<TTariffTaxi[]> {
    return this.http
      .get<TTariffTaxi[]>(`${GET_ALL_TARIFFS_TAXI}`)
      .then(x => this.process.getResponseData(x, IOTariffTaxi));
  }

  getAllTariffsPersonal(): Promise<TTariffPersonal[]> {
    return this.http
      .get<TTariffPersonal[]>(`${GET_ALL_TARIFFS_PERSONAL}`)
      .then(x => this.process.getResponseData(x, IOTariffPersonal));
  }

  getAllTariffsPublic(): Promise<TTariffPublic> {
    return this.http.get<TTariffPublic>(GET_ALL_TARIFFS_PUBLIC).then(this.process.decodeResponseData(IOTariffPublic));
  }

  getTariffById(transTypeId: string, tariffId: UUID): Promise<TTariffPublic> {
    return this.http
      .get<TTariffPublic>(GET_TARIFF, { urlParams: { transTypeId, tariffId } })
      .then(this.process.decodeResponseData(IOTariffPublic));
  }

  getTarrifCarsharing(tariffId: UUID): Promise<ITarrifCarsharing> {
    return this.http.get<ITarrifCarsharing>(GET_TARIFF_CARSHARING, { urlParams: { tariffId } }).then(el => el.data);
  }

  getContractors(contractorid: UUID): Promise<IContractors> {
    return this.http.get<IContractors>(GET_CONTRACTORS, { urlParams: { contractorid } }).then(el => el.data);
  }

  getUserAvatart(userId: string): Promise<string> {
    return this.http.get<Blob>(`${GET_USER_AVATART}${userId}`, { responseType: 'arraybuffer' }).then(x => {
      const file = new Blob([x.data]);
      return URL.createObjectURL(file);
    });
  }

  getAllCoopTrips(data: TripRequest): Promise<TTripCoop[]> {
    return this.http
      .post<TTripCoop[]>(GET_SUITABLE_COOPERATIVE_TRIPS, data)
      .then(this.process.decodeResponseData(t.array(IOTripCoop)))
      .catch(() => []);
  }

  joinCoopTrip(sharedId: string, data: TTripRequestNew): Promise<TripRequest> {
    return this.http
      .post<TripRequest>(ACCEPT_SUITABLE_COOPERATIVE_TRIP, data, { urlParams: { sharedId } })
      .then(x => this.process.getResponseData(x, TripRequest));
  }

  deleteRequest(reqId: string): Promise<number> {
    return this.http.delete(`${DELETE_REQUEST_PARAMS}`, { urlParams: { reqId } }).then(this.process.getResponseStatus);
  }

  finishTripRequest(reqId: string): Promise<number> {
    return this.http
      .post(`${FINISH_REQUEST_PARAMS}`, {}, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }

  saveFile(file: File, folder: UUID | string): Promise<SavedFileInfo> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<SavedFileInfo>(`${SAVE_FILE}`, formData, { urlParams: { folder } })
      .then(x => this.process.getResponseData(x, SavedFileInfo));
  }

  saveConfirmSuburbTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number> {
    return this.http
      .post<SavedFileInfo>(SAVE_CONFIRM_SUBURB_FILE, filesData, { urlParams: { requestId } })
      .then(x => this.process.getResponseStatus(x));
  }

  saveConfirmCardTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number> {
    return this.http
      .post<SavedFileInfo>(SAVE_CONFIRM_CARD_FILE, filesData, { urlParams: { requestId } })
      .then(x => this.process.getResponseStatus(x));
  }

  saveTravelCardRequest(data: InnerCityTransportRequestInfo): Promise<number> {
    return this.http.post(`${SAVE_TRAVEL_CARD_REQUEST}`, data, {}).then(x => this.process.getResponseStatus(x));
  }

  saveSuburbCompensation(data: InnerCityTransportRequestInfo): Promise<number> {
    return this.http.post(`${SAVE_SUBURB_COMPENSATION}`, data, {}).then(x => this.process.getResponseStatus(x));
  }

  saveCityTripCompensation(data: CityTripCompensation): Promise<number> {
    return this.http.post(`${SAVE_CITY_TRIP_COMPENSATION}`, data, {}).then(x => this.process.getResponseStatus(x));
  }

  savePublicTripRequest(data: PublicTripCompensation): Promise<unknown> {
    return this.http.post(CREATE_PUBLIC_TRIP_COMPENSATION, data, {}).then(x => x.data);
  }

  loadCoopTripsSettings(organizationId: string): Promise<TCoopTripsSettings[]> {
    return this.http
      .get<TCoopTripsSettings[]>(`${ORGANIZATIONS}/${organizationId}${SHARED_RIDE_SETTINGS}`)
      .then(this.process.decodeResponseData(t.array(IOCoopTripsSettings)));
  }

  searchNumberTrip(name): Promise<typeof IOSearchNumberTrip> {
    return this.http
      .post<typeof IOSearchNumberTrip>(GET_REQUEST_SELF, name)
      .then(this.process.getResponseData);
  }

  formatErrorWithRubles(errorText: string): string {
    const pattern = /на сумму (\d+) на дату/;

    return errorText.replace(pattern, (match, p1) => {
      const numeric = parseInt(p1);
      if (Number.isNaN(numeric)) return match;

      return `на сумму ${numeric / 100} на дату`;
    });
  }
}
