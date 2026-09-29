import type { ILogger, IResponseService, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { Dictionary } from 'lodash';
import mapKeys from 'lodash/mapKeys';
import {
  action, computed, IReactionDisposer, observable, reaction
} from 'mobx';
import { plainToNew } from 'utils';

import * as DelegatesInterface from 'stores/Delegates/Delegates.interface';
import { LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { calculateTaxiClassCost } from 'modules/CreateTripRequest/utils/utils';

import type { UUID } from '../../utils/io-ts';
import { TripRequestModel } from './models';
import { TariffPersonal } from './models/TariffPersonal.model';
import { TariffTaxi } from './models/TariffTaxi.model';
import { TripPaginationModel } from './models/TripPagination.model';
import { TripPriceModel } from './models/TripPrice.model';
import { TripSuitableModel } from './models/TripSuitable.model';
import * as TripInterface from './Trip.interface';

// TODO deprecated
@injectable()
export class DITripStore implements TripInterface.ITripStore {
  @inject(TYPES.IDelegatesService)
  private delegateService!: DelegatesInterface.IDelegatesService;

  @inject(TYPES.ITripService)
  private service!: TripInterface.ITripService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: ISelfEmployeeStore;

  @computed
  get selfEmployee(): EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @computed
  get currentTripRequest(): TripRequestModel | undefined {
    return this.tripRequestList.find(request => request.id === this.currentTripRequestId);
  }

  @observable classCosts: TripInterface.ITripTariff[] = [];

  @observable personalCarsCosts: TripInterface.ITripTariff[] = [];

  @observable tripRequestList: TripRequestModel[] = [];

  @observable nonTerminalTotalElements: number | null = null;

  @observable pagination: TripPaginationModel = {
    size: 10, totalElements: -1, totalPages: 0, number: 0,
  };

  @observable savedTripRequest?: TripRequestModel;

  @observable currentTripRequestId?: string | null;

  @observable purposes: TripInterface.TripPurpose[] = [];

  @observable tariffsTaxi: TripInterface.TTariffTaxi[] = [];

  @observable tariffsPersonal: TripInterface.TTariffPersonal[] = [];

  @observable tariffsPublic?: TripInterface.TTariffPublic;

  @observable actualTariff?: TripInterface.TTariffPublic;

  @observable suitableCooperativeTrips: TripSuitableModel[] = [];

  @observable tripStatusChanged?: boolean;

  @observable isSearchingSuitableTrip = false;

  @observable isSearchedSuitableTrip = false;

  @observable passengerLimitRemainder: LimitSharing[] = [];

  @observable delegatesByEmployeeId: Record<string, DelegatesInterface.DelegateModel[]> = {};

  @observable coopTripsSettings: TripInterface.TCoopTripsSettings[] = [];

  @observable externalPrices: TripInterface.TExternalPrices[] = [];

  private disposeCurrentTripRequestReaction?: IReactionDisposer;

  @computed
  get purposesMapped(): Dictionary<TripInterface.TripPurpose> {
    return mapKeys(this.purposes, 'id');
  }

  @computed
  get savedSuccessfully(): boolean {
    return !!this.savedTripRequest?.isExisting;
  }

  @action
  async getCosts(data: TripInterface.ITripCalculateRequest): Promise<void> {
    const classCostsResult = (await this.calculateTripCost(data)) ?? [];
    // ToDo: временное решение пока бонусы считаются на фронте
    const classCostsBonusCost = classCostsResult.find(
      tripModel => tripModel.taxiClass === TripInterface.TaxiClassEnum.ECONOMY
    )?.cost;
    this.classCosts = classCostsResult.map(tripPriceModel => {
      if (
        classCostsBonusCost
        && (tripPriceModel.taxiClass === TripInterface.TaxiClassEnum.COMFORT || tripPriceModel.taxiClass === TripInterface.TaxiClassEnum.BUSINESS)
      ) {
        // eslint-disable-next-line no-param-reassign
        tripPriceModel.bonusCost = tripPriceModel.cost - classCostsBonusCost;
      }
      return tripPriceModel;
    });
  }

  @action
  async getPersonalCarCost(data: TripInterface.ITripCalculateRequest): Promise<TripPriceModel[] | undefined> {
    return this.calculateCostForPersonalCarTrip(data);
  }

  @action.bound
  async getExternalPrices(data: TripInterface.TTripRequestNew): Promise<void> {
    this.externalPrices = (await this.calculateExternalPrices(data)) ?? [];
  }

  @action
  async clearClassCosts(): Promise<void> {
    this.classCosts = [];
  }

  @action
  async clearPersonalCarsCosts(): Promise<void> {
    this.personalCarsCosts = [];
  }

  @action
  async loadRequestList(): Promise<void> {
    this.tripRequestList = (await this.getTripRequestList(this.selfEmployee.id)) ?? [];
  }

  @action
  async loadRequestListTerminal(data: TripInterface.ITripRequestData): Promise<void> {
    [this.pagination, this.tripRequestList] = (await this.getTripRequestListTerminal(data)) ?? [{}, []];
  }

  @action
  async loadRequestListNonTerminal(data: TripInterface.ITripRequestData): Promise<void> {
    [this.pagination, this.tripRequestList, this.nonTerminalTotalElements] = (await this.getTripRequestListNonTerminal(
      data
    )) ?? [{}, []];
  }

  @action
  clearRequestList(): void {
    this.tripRequestList = [];
  }

  @action.bound
  async searchCoopTrips(requestTrip: TripInterface.TripRequest): Promise<void> {
    this.isSearchingSuitableTrip = true;
    this.suitableCooperativeTrips = (await this.getSuitableCooperativeTrips(requestTrip)) ?? [];
    this.isSearchingSuitableTrip = false;
    this.isSearchedSuitableTrip = true;
  }

  @action.bound
  clearCoopTrips(): void {
    this.suitableCooperativeTrips = [];
    this.isSearchingSuitableTrip = false;
  }

  @action
  updateTripStateById({ id, newState }: { id: string; newState: Partial<TripRequestModel> }): void {
    const targetTrip = this.tripRequestList.find(request => request.id === id);
    if (targetTrip) {
      targetTrip.updateState(newState);
    }
  }

  @action
  async saveTripRequest(data: TripInterface.TTripRequestNew): Promise<void> {
    if (this.currentTripRequest?.isExisting) {
      this.updateTripRequest({ ...this.currentTripRequest, ...data });
    } else {
      this.createTripRequest(data);
    }
  }

  @action.bound
  async joinCoopTrip(sharedId: string, data: TripInterface.TTripRequestNew): Promise<void> {
    if (data.expected) {
      // eslint-disable-next-line no-param-reassign
      data.expected.cost = calculateTaxiClassCost(this.classCosts, data.transportType, data.taxiClass).cost;
      // FIXME no-param-reassign
    }
    const trip = new TripRequestModel(await this.service.joinCoopTrip(sharedId, data));
    this.savedTripRequest = trip;
    this.tripRequestList = [...this.tripRequestList, trip];
    this.logger.toMessage('success', SYSTEM_MESSAGES.suitableTripRequestAddSuccess);
  }

  @action
  /**
   * Метод сохраняет заявку с изменением состояния
   * Неявная мутация cost обратите внимание
   */
  private async createTripRequest(data: TripInterface.TTripRequestNew): Promise<void> {
    const requestData = data;
    if (requestData.expected && requestData.transportType !== 'PERSONAL') {
      requestData.expected.cost = calculateTaxiClassCost(
        this.classCosts,
        requestData.transportType,
        requestData.taxiClass
      )?.cost;
    }

    const result = await this.service.saveTripRequest(requestData);

    try {
      if (typeof result !== 'number') {
        const trip = new TripRequestModel(result);
        this.savedTripRequest = trip;
        this.tripRequestList = [...this.tripRequestList, trip];
        this.logger.toMessage('success', SYSTEM_MESSAGES.tripRequestSavedSuccessfully);
      } else {
        this.logger.toMessage('error', SYSTEM_MESSAGES.tripRequestSavedUnsuccessfullyBL);
      }
    } catch (err) {
      // eslint-disable-next-line no-console
      console.log(err);
    }
  }

  @action.bound
  addPersonalCost(tariff: TripInterface.ITripTariff): void {
    this.personalCarsCosts.push(tariff);
  }

  @action
  private async updateTripRequest(data: TripInterface.TripRequest): Promise<void> {
    const requestData = data;

    if (requestData.expected && requestData.transportType !== 'PERSONAL') {
      requestData.expected.cost = calculateTaxiClassCost(
        this.classCosts,
        requestData.transportType,
        requestData.taxiClass
      ).cost;
    }

    try {
      const response = await this.service.editTripRequest(data, data.id);
      const isSuccess = this.process.processStatus(response, SYSTEM_MESSAGES.tripRequestSavedSuccessfully);

      if (isSuccess) {
        this.logger.toMessage('success', SYSTEM_MESSAGES.tripRequestSavedSuccessfully);
        const index = this.tripRequestList.findIndex(x => x.id === data.id);
        const trip = new TripRequestModel(data);
        this.tripRequestList[index] = trip;
        this.tripRequestList = [...this.tripRequestList];
        this.savedTripRequest = trip;
      }
    } catch (e: any) {
      const errorMessage
        = e.response && e.response.data ? e.response.data.message : SYSTEM_MESSAGES.tripRequestUpdatedUnsuccessfully;
      this.logger.toMessage('error', errorMessage);
    }
  }

  @action.bound
  async rateTripRequest(data: TripInterface.IRequestRating, reqId: string): Promise<void> {
    const response = await this.service.rateTripRequest(data, reqId);
    this.process.processStatus(response, SYSTEM_MESSAGES.tripRequestRated);
  }

  @action.bound
  async setCurrentTripRequest(requestId: string): Promise<void> {
    this.currentTripRequestId = requestId;
    const requestExistInList = Boolean(this.getTripRequestById(requestId));
    if (this.tripStatusChanged || !requestExistInList) {
      this.getTripRequest(requestId).then(
        currentTripRequest => {
          const index = this.tripRequestList.findIndex(req => req.id === requestId);
          if (index !== -1) {
            if (currentTripRequest) {
              this.tripRequestList[index] = currentTripRequest;
            } else {
              this.tripRequestList.splice(index, 1);
            }
          } else if (currentTripRequest) {
            this.tripRequestList.push(currentTripRequest);
          }
        },
        () => {
          // TODO: what to do if request failed?
        }
      );
    }
  }

  @action
  clearCurrentRequest(): void {
    this.classCosts = [];
    this.savedTripRequest = undefined;
    this.currentTripRequestId = undefined;
  }

  getTripRequestById(id: string): TripRequestModel | undefined {
    return this.tripRequestList.find(request => request.id === id);
  }

  @action
  async loadTariffs(): Promise<void> {
    this.tariffsTaxi = (await this.getTariffsTaxi()) ?? [];

    this.tariffsPersonal = (await this.getTariffsPersonal()) ?? [];

    // this.tariffsPublic = (await this.getAllTariffsPublic()) ?? [];
  }

  @action
  async loadActualTariff(transTypeId: string, tariffId: UUID): Promise<void> {
    this.actualTariff = (await this.getTariffById(transTypeId, tariffId)) as any;
  }

  @action
  async loadPurposesList(orgId: string): Promise<void> {
    this.purposes = (await this.getPurposesList(orgId)) ?? [];
  }

  @action.bound
  async finishTripRequest(reqId: string): Promise<void> {
    const status = await this.service.finishTripRequest(reqId);
    this.tripStatusChanged = this.process.processStatus(status, SYSTEM_MESSAGES.tripRequestFinish);
  }

  @action
  async saveFile(file: File, folder: UUID | string): Promise<TripInterface.SavedFileInfo> {
    return this.service.saveFile(file, folder);
  }

  @action
  async saveConfirmSuburbTripFile(requestId: string | UUID, fileData: TripInterface.SavedFileInfo[]): Promise<number> {
    return this.service.saveConfirmSuburbTripFile(requestId, fileData);
  }

  @action
  async saveConfirmCardTripFile(requestId: string | UUID, fileData: TripInterface.SavedFileInfo[]): Promise<number> {
    return this.service.saveConfirmCardTripFile(requestId, fileData);
  }

  @action
  async saveTravelCardRequest(data: TripInterface.InnerCityTransportRequestInfo): Promise<number> {
    return this.service.saveTravelCardRequest(data);
  }

  @action
  async saveSuburbCompensation(data: TripInterface.InnerCityTransportRequestInfo): Promise<number> {
    return this.service.saveSuburbCompensation(data);
  }

  @action
  async saveCityTripCompensation(data: TripInterface.CityTripCompensation): Promise<number> {
    return this.service.saveCityTripCompensation(data);
  }

  @action
  async savePublicTripRequest(data: TripInterface.PublicTripCompensation): Promise<number> {
    return this.service.savePublicTripRequest(data);
  }

  @action.bound
  async loadCoopTripsSettings(organizationId: string): Promise<void> {
    this.coopTripsSettings = await this.service.loadCoopTripsSettings(organizationId);
  }

  private async getPurposesList(orgId: string): Promise<TripInterface.TripPurpose[] | undefined> {
    return this.service.getAllPurposesByEmployee(orgId);
  }

  private async getTariffsTaxi(): Promise<TripInterface.TTariffTaxi[] | undefined> {
    const response = await this.service.getAllTariffsTaxi();
    return plainToNew<TariffTaxi[]>(
      TariffTaxi,
      response.filter(t => t.active)
    );
  }

  private async getTariffsPersonal(): Promise<TripInterface.TTariffPersonal[] | undefined> {
    const response = await this.service.getAllTariffsPersonal();
    return plainToNew<TariffPersonal[]>(TariffPersonal, response);
  }

  private async getAllTariffsPublic(): Promise<TripInterface.TTariffPublic | undefined> {
    const response = await this.service.getAllTariffsPublic();
    this.tariffsPublic = response;
    return response;
  }

  private async getTariffById(transTypeId: string, tariffId: UUID): Promise<TripInterface.TTariffPublic | undefined> {
    const response = await this.service.getTariffById(transTypeId, tariffId);
    this.actualTariff = response as any;
    return response;
  }

  private async getTripRequestList(passengerId = ''): Promise<TripRequestModel[] | undefined> {
    const response = await this.service.getTripRequestList(passengerId);
    return plainToNew(TripRequestModel, response);
  }

  private async getTripRequestListTerminal(data: TripInterface.ITripRequestData): Promise<[TripPaginationModel, TripRequestModel[]]> {
    const response = await this.service.getTripRequestListTerminal(data);

    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<TripRequestModel[]>(TripRequestModel, response.content);
    return [pagination, list];
  }

  private async getTripRequestListNonTerminal(
    data: TripInterface.ITripRequestData
  ): Promise<[TripPaginationModel, TripRequestModel[], number]> {
    const response = await this.service.getTripRequestListNonTerminal(data);

    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<TripRequestModel[]>(TripRequestModel, response.content);
    return [pagination, list, response.totalElements];
  }

  private async getTripRequest(requestId: string): Promise<TripRequestModel | undefined> {
    const response = await this.service.getTripRequest(requestId);
    return plainToNew(TripRequestModel, response);
  }

  private async getSuitableCooperativeTrips(tripRequest: TripInterface.TripRequest): Promise<TripSuitableModel[]> {
    const response = await this.service.getAllCoopTrips(tripRequest);
    const trips: TripSuitableModel[] = plainToNew(TripSuitableModel, response);
    if (tripRequest.transportType === TransportTypeEnum.PERSONAL) {
      trips.forEach(item => {
        item.setIsPersonalTrue();
      });
    }
    return trips;
  }

  @action.bound
  private async calculateTripCost(data: TripInterface.ITripCalculateRequest): Promise<TripPriceModel[] | undefined> {
    const response = await this.service.calculateTripCost(data, this.loadActualTariff.bind(this));
    return plainToNew(TripPriceModel, response);
  }

  private async calculateCostForPersonalCarTrip(data: TripInterface.ITripCalculateRequest): Promise<TripPriceModel[] | undefined> {
    const response = await this.service.calculateCostForPersonalCarTrip(data);
    return plainToNew(TripPriceModel, response);
  }

  private async calculateExternalPrices(data: TripInterface.TTripRequestNew): Promise<TripInterface.TExternalPrices[]> {
    return this.service.calculateExternalPrices(data);
  }

  initStore(): void {
    const { orgId } = this.selfStore;

    this.loadRequestListNonTerminal({
      pageSetting: {
        page: 0,
        size: 1,
      },
    });
    this.loadPurposesList(orgId);
    this.loadTariffs();
    this.loadCoopTripsSettings(orgId);

    // kill previous current trip request reaction
    if (this.disposeCurrentTripRequestReaction) {
      this.disposeCurrentTripRequestReaction();
    }

    this.disposeCurrentTripRequestReaction = reaction(
      () => this.currentTripRequest,
      async currentTripRequest => {
        const { approvedBy } = currentTripRequest || {};
        if (
          approvedBy
          && approvedBy.organizationId
          && approvedBy.departmentId
          && approvedBy.id
          && !this.delegatesByEmployeeId[approvedBy.id]
        ) {
          // FIXME удалить этот запрос, сейчас можно использовать
          // хук useGetDelegates
          const delegates = await this.delegateService.getDelegates({
            orgId: approvedBy.organizationId,
            depId: approvedBy.departmentId,
            supId: approvedBy.id,
          });
          this.delegatesByEmployeeId[approvedBy.id] = delegates;
        }
      },
      { fireImmediately: true }
    );

    reaction(
      () => ({ requests: [...this.tripRequestList], purposes: [...this.purposes] }),
      ({ requests, purposes }) => {
        requests.forEach(req => {
          if (!purposes.some(purpose => purpose.id === req.purpose.id)) {
            req.purpose.label = `${req.purpose.label} (Не активна)`;
          }
        });
      }
    );
  }
}
