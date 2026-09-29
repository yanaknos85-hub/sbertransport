import type { IHttpService, ILogger, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, observable } from 'mobx';

import { SYSTEM_MESSAGES } from '../../constants/constants.app';
import {
  EXCHANGE_ACCEPT,
  EXCHANGE_AVAILABLE,
  EXCHANGE_DENY,
  EXCHANGE_UPDATE_STATUS
} from '../../modules/Exchange/api/constants';
import { ExchangeResponse } from '../../modules/Exchange/types';
import type { IExchangeStore } from './Exchange.interface';
import { PaginationModel } from './models/Pagination.model';
import type {
  DesiredDateRange, ListType, PageSetting, SortSetting
} from './types';
import { SortProperty, SortPropertyRusType } from './types';

@injectable()
export class DIExchangeStore implements IExchangeStore {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @observable
    pagination: PaginationModel = {
      size: 10, totalElements: -1, totalPages: 0, number: 0,
    };

  @observable
    searchId: string | null = null;

  @observable
  private initialSortingOrders = 'Сначала новые';

  @observable
  private initialPageSetting: PageSetting = {
      page: 0,
      size: 10,
    };

  @observable
  private initialSortSetting: { directionAsc: boolean; property: SortProperty } = {
      directionAsc: false,
      property: SortProperty.CREATION_DATE,
    };

  @observable
    sortingOrders = 'Сначала новые';

  @observable
    stateSorting: Record<string, string> = {
      cargoRequestList: 'Сначала новые',
    };

  @observable
    pageSetting: PageSetting = {
      page: 0,
      size: 10,
    };

  @observable
    sortSetting: { directionAsc: boolean; property: SortProperty } = {
      directionAsc: false,
      property: SortProperty.CREATION_DATE,
    };

  @observable
    exchangeRequest: ExchangeResponse | {} = {};

  @action.bound
  private getAvailableListService(type: ListType, pageSetting: PageSetting, sortSetting: SortSetting) {
    return this.http
      .post<ExchangeResponse>(EXCHANGE_AVAILABLE, { pageSetting, sortSetting }, { urlParams: { type } })
      .then(this.process.getResponseData);
  }

  @action.bound
  private denyExchangeService(id: string) {
    return this.http
      .put<ExchangeResponse>(EXCHANGE_DENY, {}, { urlParams: { id } })
      .then(this.process.getResponseData);
  }

  @action.bound
  private takeToWorkExchangeService(id: string) {
    return this.http
      .put<ExchangeResponse>(EXCHANGE_ACCEPT, {}, { urlParams: { id } })
      .then(this.process.getResponseData);
  }

  @action.bound
  async takeToWorkExchange(id: string, type: ListType): Promise<void> {
    try {
      await this.takeToWorkExchangeService(id);
      this.logger.toMessage('success', SYSTEM_MESSAGES.exchangeTakeToWorkSuccess);
      this.exchangeRequest = await this.getAvailableListService(type, this.pageSetting, this.sortSetting);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  async denyExchange(id: string, type: ListType): Promise<void> {
    try {
      await this.denyExchangeService(id);
      this.logger.toMessage('success', SYSTEM_MESSAGES.exchangeDeclineSuccess);
      this.exchangeRequest = await this.getAvailableListService(type, this.pageSetting, this.sortSetting);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  private getAvailableListFilterService(type: ListType, pageSetting: PageSetting, sortSetting: SortSetting, addressFrom: string, addressTo: string, desiredDateRange?: { start: string; end: string }) {
    return this.http
      .post<ExchangeResponse>(EXCHANGE_AVAILABLE, {
        pageSetting, sortSetting, addressFrom, addressTo, desiredDateRange,
      }, { urlParams: { type } })
      .then(this.process.getResponseData);
  }

  @action.bound
  async getFilterAvailableList(
    type: ListType,
    addressFrom: string,
    addressTo: string,
    desiredDateRange?: DesiredDateRange): Promise<void> {
    try {
      this.exchangeRequest = await this.getAvailableListFilterService(type, this.pageSetting, this.sortSetting, addressFrom, addressTo, desiredDateRange);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  private getRequestByIdService(type: ListType, id) {
    return this.exchangeRequest = this.http
      .post<ExchangeResponse>(EXCHANGE_AVAILABLE, { humanReadableId: id }, { urlParams: { type } })
      .then(this.process.getResponseData);
  }

  @action.bound
  async getRequestById(
    type: ListType,
    id: string
  ): Promise<void> {
    try {
      this.exchangeRequest = await this.getRequestByIdService(type, id);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  async getAvailableList(
    type: ListType,
    pageSetting: PageSetting,
    sortSetting: SortSetting) {
    try {
      this.exchangeRequest = await this.getAvailableListService(type, pageSetting, sortSetting);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  async getAvailableListWithFilters(
    type: ListType,
    pageSetting: PageSetting,
    sortSetting: SortSetting,
    addressFrom?: string,
    addressTo?: string,
    desiredDateRange?: { start: string; end: string }
  ): Promise<void> {
    try {
      const params: any = { pageSetting, sortSetting };
      if (addressFrom) params.addressFrom = addressFrom;
      if (addressTo) params.addressTo = addressTo;
      if (desiredDateRange) params.desiredDateRange = desiredDateRange;

      this.exchangeRequest = await this.http
        .post<ExchangeResponse>(EXCHANGE_AVAILABLE, params, { urlParams: { type } })
        .then(this.process.getResponseData);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  private updateRouteStatusService(id: string, status: string) {
    return this.http
      .post<ExchangeResponse>(EXCHANGE_UPDATE_STATUS, {}, { urlParams: { id, status } })
      .then(this.process.getResponseData);
  }

  @action.bound
  async updateRouteStatusExchange(id: string, status: string, type: ListType): Promise<void> {
    try {
      await this.updateRouteStatusService(id, status);
      this.exchangeRequest = await this.getAvailableListService(type, this.pageSetting, this.sortSetting);
    } catch (e: any) {
      this.logger.toMessage('error', e.response.data.message);
    }
  }

  @action.bound
  setPageSetting(settings: PageSetting): void {
    this.pageSetting = {
      page: settings.page,
      size: settings.size,
    };
  }

  @action.bound
  setSortSetting(directionAsc: boolean, property: SortProperty): void {
    this.sortSetting = {
      directionAsc: !directionAsc,
      property: property,
    };
  }

  @action.bound
  resetSettings(): void {
    this.sortingOrders = this.initialSortingOrders;
    this.pageSetting = this.initialPageSetting;
    this.sortSetting = this.initialSortSetting;
  }

  @action.bound
  resetSortingOrders(): void {
    this.sortingOrders = SortPropertyRusType[SortProperty.CREATION_DATE].DESC;
  }

  @action.bound
  setSortingOrders(directionAsc: boolean, property: SortProperty): void {
    switch (property) {
      case SortProperty.CREATION_DATE:
        if (directionAsc) {
          this.sortingOrders = SortPropertyRusType[SortProperty.CREATION_DATE].ASC;
        } else {
          this.sortingOrders = SortPropertyRusType[SortProperty.CREATION_DATE].DESC;
        }
        break;
      default:
        break;
    }
  }
}
