import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import {
  CARGO_ACCEPT_RECEIVE,
  MOCKED_API_PREFIX,
  MULTIPLE_CARGO_APPROVABLE_NON_TERMINAL,
  MULTIPLE_CARGO_APPROVABLE_TERMINAL,
  MULTIPLE_CARGO_APPROVE,
  MULTIPLE_CARGO_CANCEL,
  MULTIPLE_CARGO_DECLINE,
  MULTIPLE_CARGO_HISTORY,
  MULTIPLE_CARGO_REQUEST,
  MULTIPLE_CARGO_SELF_NON_TERMINAL,
  MULTIPLE_CARGO_SELF_TERMINAL,
  MULTIPLE_REGULAR_CARGO_APPROVABLE_NON_TERMINAL,
  MULTIPLE_REGULAR_CARGO_APPROVABLE_TERMINAL,
  MULTIPLE_REGULAR_CARGO_APPROVE,
  MULTIPLE_REGULAR_CARGO_CANCEL,
  MULTIPLE_REGULAR_CARGO_CARGO_HISTORY,
  MULTIPLE_REGULAR_CARGO_DECLINE,
  MULTIPLE_REGULAR_CARGO_REQUEST,
  MULTIPLE_REGULAR_CARGO_SELF_NON_TERMINAL,
  MULTIPLE_REGULAR_CARGO_SELF_TERMINAL
} from 'constants/constants.api';

import { UUID } from '../../utils/io-ts';
import { TripPaginationModel } from '../Trip/models/TripPagination.model';
import { ICargoRequestSearch, ICargosService } from './Cargos.interface';
import { CargoRegularRequestModel } from './models/CargoRegularRequest.model';
import { CargoRequestModel } from './models/CargoRequest.model';
import { CargoHistoryType, CargoRequestType } from './types';

@injectable()
export class DICargosService implements ICargosService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getMultipleRequestListTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(`${MOCKED_API_PREFIX}/${MULTIPLE_CARGO_SELF_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getMultipleRequestListNonTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(
        `${MOCKED_API_PREFIX}/${MULTIPLE_CARGO_SELF_NON_TERMINAL}`,
        data
      )
      .then(this.process.getResponseData);
  }

  getApproveMultipleListTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(`${MOCKED_API_PREFIX}${MULTIPLE_CARGO_APPROVABLE_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getApproveMultipleListNonTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(`${MOCKED_API_PREFIX}${MULTIPLE_CARGO_APPROVABLE_NON_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getMultipleRegularApproveListTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(`${MULTIPLE_REGULAR_CARGO_APPROVABLE_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getMultipleRegularApproveListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(`${MULTIPLE_REGULAR_CARGO_APPROVABLE_NON_TERMINAL}`, data)
      .then(this.process.getResponseData);
  }

  getMultipleRegularRequestListTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(
        `${MOCKED_API_PREFIX}/${MULTIPLE_REGULAR_CARGO_SELF_TERMINAL}`,
        data
      )
      .then(this.process.getResponseData);
  }

  getMultipleRegularRequestListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }> {
    return this.http
      .post<TripPaginationModel & { content: CargoRequestType[] }>(
        `${MOCKED_API_PREFIX}/${MULTIPLE_REGULAR_CARGO_SELF_NON_TERMINAL}`,
        data
      )
      .then(this.process.getResponseData);
  }

  getMultipleRequestById(rqUuid: UUID): Promise<CargoRequestModel> {
    return this.http
      .get<CargoRequestModel>(`${MOCKED_API_PREFIX}/${MULTIPLE_CARGO_REQUEST}`, { urlParams: { rqUuid } })
      .then(this.process.getResponseData);
  }

  getMultipleRegularRequestById(rqUuid: UUID): Promise<CargoRegularRequestModel> {
    return this.http
      .get<CargoRegularRequestModel>(`${MOCKED_API_PREFIX}/${MULTIPLE_REGULAR_CARGO_REQUEST}`, { urlParams: { rqUuid } })
      .then(this.process.getResponseData);
  }

  getMultipleCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]> {
    return this.http
      .get<CargoHistoryType[]>(`${MOCKED_API_PREFIX}/${MULTIPLE_CARGO_HISTORY}`, { urlParams: { rqUuid } })
      .then(this.process.getResponseData);
  }

  getMultipleRegularCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]> {
    return this.http
      .get<CargoHistoryType[]>(`${MOCKED_API_PREFIX}/${MULTIPLE_REGULAR_CARGO_CARGO_HISTORY}`, { urlParams: { rqUuid } })
      .then(this.process.getResponseData);
  }

  async cancelRequestMultiple(rqUuid: UUID, reason: string, code: number, field: string, value: string): Promise<void> {
    return this.http
      .put<any>(
        `${MULTIPLE_CARGO_CANCEL}`,
        {
          reason,
          code,
          field,
          value,
        },
        { urlParams: { rqUuid } }
      )
      .then(this.process.getResponseData);
  }

  async cancelMultipleRegularRequest(rqUuid: UUID): Promise<void> {
    return this.http
      .put<any>(
        `${MULTIPLE_REGULAR_CARGO_CANCEL}`,
        {
          code: 800,
        },
        { urlParams: { rqUuid } }
      )
      .then(this.process.getResponseData);
  }

  /* TODO исправить any, обсудить с бэком актуализацию данных */
  async approveRequest(rqUuids: string[]): Promise<any> {
    const data = [...rqUuids];
    return this.http
      .put<void>(
        `${MULTIPLE_CARGO_APPROVE}`,
        {
          requestIds: data,
        }
      )
      .then(this.process.getResponseData);
  }

  async declineRequest(rqUuids: string[]): Promise<any> {
    return this.http
      .put<any>(
        `${MULTIPLE_CARGO_DECLINE}`,
        {
          requestIds: rqUuids,
          reason: 'string',
          code: 0,
        }
      )
      .then(this.process.getResponseData);
  }

  // Согласование регулярных заявок
  async approveRegularRequest(rqUuid: string[]): Promise<any> {
    const data = [...rqUuid];
    return this.http
      .put<void>(
        `${MULTIPLE_REGULAR_CARGO_APPROVE}`,
        {
          templateIds: data,
        }
      )
      .then(this.process.getResponseData);
  }

  // Отклонение регулярной заявки
  async declineMultipleRegularRequest(rqUuid: string[], reason: string): Promise<void> {
    return this.http
      .put<any>(
        `${MULTIPLE_REGULAR_CARGO_DECLINE}`,
        {
          templateIds: rqUuid,
          reason,
        }
      )
      .then(this.process.getResponseData);
  }

  // Подтверждение получения груза
  async confirmationReceivingCargo(
    rqUuid: string,
    occupiedPlacesCountFact: number,
    comment: string
  ): Promise<void> {
    return this.http
      .put<void>(
        `${CARGO_ACCEPT_RECEIVE}`,
        {
          occupiedPlacesCountFact,
          comment,
        },
        {
          urlParams: { rqUuid },
        }
      )
      .then(this.process.getResponseData);
  }
}
