// @ts-nocheck
import { ILogger } from '@sber-sbertransport/mf-core';
import { notification } from 'antd';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, observable } from 'mobx';
import {
  CHANGE_STATUS_DESCRIPTION,
  CONFIRMATION_RECEIVING_CARGO,
  PLACES_DECLENSION_DESCRIPTION,
  SOMETHING_WRONG_TITLE
} from 'shared/constants/constants';

import { UUID } from 'utils/io-ts';
import { declension, plainToNew } from 'utils/Misc';
import {
  activeTabFilter,
  KeyFilterState,
  KeyFilterStateMain,
  KeyFilterStateMainApproval
} from 'modules/CargosMultiple/CargoTabFilters/types';

import { ExternalEmployee } from '../Cargo/Cargo.interface';
import { TripPaginationModel } from '../Trip/models/TripPagination.model';
import { ICargoRequestSearch, ICargosService, ICargosStore } from './Cargos.interface';
import { CargoRequestModel } from './models/CargoRequest.model';
import {
  CargoHistoryType,
  Filters,
  FilterSettingsType,
  RangePickerArg,
  Settings,
  SortProperty
} from './types';

@injectable()
export class DICargosStore implements ICargosStore {
  @inject(TYPES.ICargosService)
  private service!: ICargosService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    pagination: TripPaginationModel = {
      size: 10, totalElements: -1, totalPages: 0, number: 0,
    };

  @observable
    paginationRegular: TripPaginationModel = {
      size: 10, totalElements: -1, totalPages: 0, number: 0,
    };

  @observable
    cargoRequestList: CargoRequestModel[] = [];

  @observable
    cargoApprovalList: CargoRequestModel[] = [];

  @observable
    cargoMultipleApprovalList: CargoRequestModel[] = [];

  @observable
    cargoRegularRequestList: CargoRequestModel[] = [];

  @observable
    cargoRegularApprovalList: CargoRequestModel[] = [];

  @observable
    cargoMultipleRegularApprovalList: CargoRequestModel[] = [];

  @observable
    nonTerminalTotalElements: number | null = null;

  @observable
    nonTerminalRegularTotalElements: number | null = null;

  @observable
    nonTerminalMultipleTotalElements: number | null = null;

  @observable
    nonTerminalMultipleRegularTotalElements: number | null = null;

  @observable
    cargoRequest: CargoRequestModel | undefined;

  @observable
    cargoMultipleRequest: CargoRequestModel | undefined;

  @observable
    cargoRegularRequest: CargoRequestModel | undefined;

  @observable
    cargoMultipleRegularRequest: CargoRequestModel | undefined;

  @observable
    cargoHistory: CargoHistoryType[] = [];

  @observable
    cargoMultipleHistory: CargoHistoryType[] = [];

  @observable
    cargoRegularHistory: CargoHistoryType[] = [];

  @observable
    cargoMultipleRegularHistory: CargoHistoryType[] = [];

  @observable
    externalSender: ExternalEmployee = '';

  @observable
    externalRecipient: ExternalEmployee = '';

  @observable
    keyFilterState: KeyFilterState = KeyFilterState.once;

  @observable
    keyFilterStateMain: KeyFilterStateMain = KeyFilterStateMain.regular;

  @observable
    keyFilterStateMainApproval: KeyFilterStateMainApproval = KeyFilterStateMainApproval.delivery;

  @observable
    checkedItem = '';

  @observable
    checkedListApproval: CargoRequestModel[] = [];

  @observable
    isCheckAll = false;

  @observable
    isIndeterminate = false;

  @observable
    tabFilterName: Record<KeyFilterState, string> = {
      [KeyFilterState.once]: '',
      [KeyFilterState.regular]: activeTabFilter.authorIdRegular,
    };

  @observable
    tabFilterNameMain: Record<KeyFilterState, string> = {
      [KeyFilterState.once]: '',
      [KeyFilterState.regular]: '',
    };

  @observable
    activeTabFilter: Record<KeyFilterState, string> = {
      [KeyFilterState.once]: activeTabFilter.all,
      [KeyFilterState.regular]: activeTabFilter.authorIdRegular,
    };

  @observable
    activeTabFilterMain: Record<KeyFilterStateMain, string> = {
      [KeyFilterStateMain.once]: KeyFilterStateMain.once,
      [KeyFilterStateMain.regular]: KeyFilterStateMain.regular,
    };

  @observable
    initialTabFilterName = Filters.all;

  @observable
    initialPageSetting: { page: number; size: number } = {
      page: 0,
      size: 10,
    };

  @observable
    initialSortSetting: { directionAsc: boolean; property: SortProperty; sortingOrders: string } = {
      directionAsc: false,
      property: SortProperty.CREATION_DATE,
      sortingOrders: 'По времени создания: По убыванию',
    };

  listPathName = '';

  @observable
    sortingOrders = 'По времени создания: По убыванию';

  @observable
    nameList = '';

  @observable
    stateSorting: Record<string, string> = {
      cargoRequestList: 'По времени создания: По убыванию',
      cargoApprovalList: 'По времени создания: По убыванию',
      cargoRegularRequestList: 'По времени создания: По убыванию',
      cargoRegularApprovalList: 'По времени создания: По убыванию',
      cargoMultipleApprovalList: 'По времени создания: По убыванию',
    };

  @observable
    pageSetting: { page: number; size: number } = this.initialPageSetting;

  @observable
    desiredDateRange: RangePickerArg = [null, null];

  @observable
    sortMapping: Record<SortProperty, { true: string; false: string }> = {
      [SortProperty.REQUEST_HUMAN_ID]: {
        true: 'По порядковому номеру заявки: По возрастанию',
        false: 'По порядковому номеру заявки: По убыванию',
      },
      [SortProperty.CREATION_DATE]: {
        true: 'По времени создания: По возрастанию',
        false: 'По времени создания: По убыванию',
      },
      [SortProperty.DESIRED_DATE]: {
        true: 'По сроку доставки: По возрастанию',
        false: 'По сроку доставки: По убыванию',
      },
      [SortProperty.EXPECTED_COST]: {
        true: 'По цене: По возрастанию',
        false: 'По цене: По убыванию',
      },
    };

  @action.bound
  setDesiredDateRange(desiredDateRange: RangePickerArg): void {
    this.desiredDateRange = desiredDateRange;
  }

  createSettings(): Settings {
    return {
      sortSetting: {
        directionAsc: false, property: SortProperty.CREATION_DATE, sortingOrders: 'По времени создания: По убыванию',
      },
    };
  }

  @observable
    settings = {
      cargoRequestList: {
        active: { ...this.createSettings(), tabFilterName: this.initialTabFilterName },
        final: { ...this.createSettings(), tabFilterName: this.initialTabFilterName },
      },
      cargoRegularRequestList: {
        active: this.createSettings(),
        final: this.createSettings(),
      },
      cargoMultipleApprovalList: {
        active: this.createSettings(),
        final: this.createSettings(),
      },
    };

  @observable
    filterSettings: FilterSettingsType = {};

  @action.bound
  setFilterSettings(filterSettings: FilterSettingsType): void {
    this.filterSettings = {
      ...this.filterSettings,
      ...filterSettings,
    };
  }

  @action.bound
  resetFilterSettings(): void {
    this.filterSettings = {};
  }

  @action.bound
  setActiveSettings(nameList: string, activeTab: string, updatedSettings: Partial<Settings>) {
    this.settings[nameList][activeTab] = {
      ...this.settings[nameList][activeTab],
      ...updatedSettings,
    };
  }

  @action.bound
  setCheckedItem(value: string): void {
    this.checkedItem = value;
  }

  @action.bound
  setCheckedListApproval(list: CargoRequestModel[]) {
    this.checkedListApproval = list;
  }

  @action.bound
  setIsCheckAll(value: boolean): void {
    this.isCheckAll = value;
  }

  @action.bound
  setIsIndeterminate(value: boolean): void {
    this.isIndeterminate = value;
  }

  @action.bound
  setCheckedListRoutesById(id: UUID) {
    this.checkedListApproval = this.checkedListApproval.filter(item => item.id !== id);
  }

  @action.bound
  setPageSetting(settings: { page: number; size: number }): void {
    this.pageSetting = {
      page: settings.page,
      size: settings.size,
    };
  }

  @action.bound
  resetSettings(): void {
    this.pageSetting = this.initialPageSetting;
  }

  @action.bound
  resetListPathName(): void {
    this.listPathName = '';
  }

  @action.bound
  setListPathName(pathName: string): void {
    this.listPathName = pathName;
  }

  @action.bound
  resetSortingOrders(): void {
    this.sortingOrders = 'По времени создания: По убыванию';
  }

  @action.bound
  setNameList(nameList: string): void {
    this.nameList = nameList;
  }

  @action.bound
  setSortingOrders(directionAsc: boolean, property: SortProperty): void {
    switch (property) {
      case SortProperty.REQUEST_HUMAN_ID:
        if (directionAsc) {
          this.sortingOrders = 'По порядковому номеру заявки: По возрастанию';
        } else {
          this.sortingOrders = 'По порядковому номеру заявки: По убыванию';
        }
        break;
      case SortProperty.CREATION_DATE:
        if (directionAsc) {
          this.sortingOrders = 'По времени создания: По возрастанию';
        } else {
          this.sortingOrders = 'По времени создания: По убыванию';
        }
        break;
      case SortProperty.DESIRED_DATE:
        if (directionAsc) {
          this.sortingOrders = 'По сроку доставки: По возрастанию';
        } else {
          this.sortingOrders = 'По сроку доставки: По убыванию';
        }
        break;
      case SortProperty.EXPECTED_COST:
        if (directionAsc) {
          this.sortingOrders = 'По цене: По возрастанию';
        } else {
          this.sortingOrders = 'По цене: По убыванию';
        }
        break;
      default:
        break;
    }
  }

  @action
  setTabFilterNameMain(name: string) {
    this.tabFilterNameMain[this.keyFilterStateMain] = name;
  }

  @action
  setKeyFilterState(isRegular?: boolean): void {
    this.keyFilterState = isRegular ? KeyFilterState.regular : KeyFilterState.once;
  }

  @action
  setKeyFilterStateMain(isRegular?: boolean): void {
    this.keyFilterStateMain = isRegular ? KeyFilterStateMain.regular : KeyFilterStateMain.once;
  }

  @action
  setKeyFilterStateMainApproval(isCompensation?: boolean): void {
    this.keyFilterStateMainApproval = isCompensation ? KeyFilterStateMainApproval.compensation : KeyFilterStateMainApproval.delivery;
  }

  @action
  setActiveTabFilterMain(tab: string): void {
    this.activeTabFilterMain[this.keyFilterStateMain] = tab;
  }

  @action
  async getMultipleRequestListTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.pagination, this.cargoRequestList] = (await this.getMultipleCargoRequestListTerminal(data)) ?? [{}, []];
  }

  @action
  async getMultipleRequestListNonTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.pagination, this.cargoRequestList, this.nonTerminalTotalElements]
      = (await this.getMultipleCargoRequestListNonTerminal(data)) ?? [{}, []];
  }

  @action
  async getMultipleApproveListTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.pagination, this.cargoMultipleApprovalList] = (await this.getCargoMultipleApproveListTerminal(data)) ?? [{}, []];
  }

  @action
  async getMultipleRegularApproveListTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.paginationRegular, this.cargoMultipleApprovalList] = (await this.getMultipleRegularCargoApproveListTerminal(data)) ?? [
      {},
      [],
    ];
  }

  @action
  async getMultipleApproveListNonTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.pagination, this.cargoMultipleApprovalList, this.nonTerminalMultipleTotalElements]
      = (await this.getCargoMultipleApproveListNonTerminal(data)) ?? [{}, []];
  }

  resetApprovalJournalFilters(nameList, activeTab: string): void {
    this.setPageSetting({ ...this.initialPageSetting });
    this.setActiveSettings(nameList, activeTab, {
      tabFilterName: this.initialTabFilterName,
    });
  }

  @action
  async getMultipleRegularApproveListNonTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.paginationRegular, this.cargoMultipleApprovalList, this.nonTerminalMultipleRegularTotalElements]
      = (await this.getMultipleRegularCargoApproveListNonTerminal(data)) ?? [{}, []];
  }

  @action
  async getMultipleRegularRequestListTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.paginationRegular, this.cargoRegularRequestList] = (await this.getMultipleCargoRequestRegularListTerminal(data)) ?? [
      {},
      [],
    ];
  }

  @action
  async getMultipleRegularRequestListNonTerminal(data: ICargoRequestSearch): Promise<void> {
    [this.paginationRegular, this.cargoRegularRequestList, this.nonTerminalRegularTotalElements]
      = (await this.getMultipleCargoRequestRegularListNonTerminal(data)) ?? [{}, []];
  }

  @action
  async getMultipleRequestById(rqUuid: UUID): Promise<void> {
    this.cargoMultipleRequest = await this.getMultipleCargoRequest(rqUuid);
  }

  @action
  async getMultipleRegularRequestById(rqUuid: UUID): Promise<void> {
    this.cargoMultipleRegularRequest = await this.getMultipleCargoRegularRequest(rqUuid);
  }

  @action
  async getMultipleStatusHistoryById(rqUuid: UUID): Promise<void> {
    this.cargoMultipleHistory = await this.getMultipleCargoHistoryById(rqUuid);
  }

  @action
  async getMultipleRegularStatusHistoryById(rqUuid: UUID): Promise<void> {
    this.cargoMultipleRegularHistory = await this.getMultipleRegularCargoHistoryById(rqUuid);
  }

  @action.bound
  async cancelRequest(rqUuid: string, reason: string, code: number, field: string, value: string): Promise<boolean> {
    try {
      await this.service.cancelRequestMultiple(rqUuid, reason, code, field, value);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', e.response.data.message);
      return false;
    }
  }

  @action.bound
  async cancelRegularRequest(rqUuid: string, reason: string, code: number, field: string, value: string): Promise<boolean> {
    try {
      await this.service.cancelMultipleRegularRequest(rqUuid, reason, code, field, value);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', e.response.data.message);
      return false;
    }
  }

  @action.bound
  async approveRequest(rqUuid: string[]): Promise<boolean> {
    try {
      await this.service.approveRequest(rqUuid);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }

  @action.bound
  async declineRequest(rqUuid: string[]): Promise<boolean> {
    try {
      await this.service.declineRequest(rqUuid);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }

  @action.bound
  async approveRegularRequest(rqUuid: string[]): Promise<boolean> {
    try {
      await this.service.approveRegularRequest(rqUuid);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }

  @action.bound
  async declineMultipleRegularRequest(rqUuids: string[], reason: string): Promise<boolean> {
    try {
      await this.service.declineMultipleRegularRequest(rqUuids, reason);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (e) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }

  private async getMultipleCargoRequestListTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[]]> {
    const response = await this.service.getMultipleRequestListTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list];
  }

  private async getMultipleCargoRequestListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[], number]> {
    const response = await this.service.getMultipleRequestListNonTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list, response.totalElements];
  }

  private async getCargoMultipleApproveListTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[]]> {
    const response = await this.service.getApproveMultipleListTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list];
  }

  private async getMultipleRegularCargoApproveListTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[]]> {
    const response = await this.service.getMultipleRegularApproveListTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list];
  }

  private async getMultipleCargoRequestRegularListTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[]]> {
    const response = await this.service.getMultipleRegularRequestListTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list];
  }

  private async getCargoMultipleApproveListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[], number]> {
    const response = await this.service.getApproveMultipleListNonTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list, response.totalElements];
  }

  private async getMultipleRegularCargoApproveListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[], number]> {
    const response = await this.service.getMultipleRegularApproveListNonTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list, response.totalElements];
  }

  private async getMultipleCargoRequestRegularListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<[TripPaginationModel, CargoRequestModel[], number]> {
    const response = await this.service.getMultipleRegularRequestListNonTerminal(data);
    const pagination = plainToNew<TripPaginationModel>(TripPaginationModel, response);
    const list = plainToNew<CargoRequestModel[]>(CargoRequestModel, response.content);
    return [pagination, list, response.totalElements];
  }

  private async getMultipleCargoRequest(rqUuid: UUID): Promise<CargoRequestModel> {
    const response = await this.service.getMultipleRequestById(rqUuid);
    return plainToNew<CargoRequestModel>(CargoRequestModel, response);
  }

  private async getMultipleCargoRegularRequest(rqUuid: UUID): Promise<CargoRequestModel> {
    const response = await this.service.getMultipleRegularRequestById(rqUuid);
    let destructuringResponse;

    if (!response?.template?.sender?.id) {
      this.externalSender = response.template.sender;
    }
    if (!response?.template?.recipient?.id) {
      this.externalRecipient = response.template.recipient;
    }

    if (response) {
      destructuringResponse = {
        humanReadableId: response.humanReadableId,
        id: response.id,
        period: response.period,
        ...response.template,
        active: response.active,
        creationTime: response.creationTime,
        status: response.status,
        approvalState: response.approvalState,
        approvedBy: response.approvedBy,
        allCost: response.allCost,
        totalCost: response.totalCost,
      } as CargoRequestModel;
    }

    return plainToNew<CargoRequestModel>(CargoRequestModel, destructuringResponse);
  }

  private async getMultipleCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]> {
    return this.service.getMultipleCargoHistoryById(rqUuid);
  }

  private async getMultipleRegularCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]> {
    return this.service.getMultipleRegularCargoHistoryById(rqUuid);
  }

  // Подтверждение получения заявки
  @action.bound
  async confirmationReceivingCargo(rqUuid: string, occupiedPlacesCountFact: number, comment: string): Promise<boolean> {
    try {
      await this.service.confirmationReceivingCargo(rqUuid, occupiedPlacesCountFact, comment);
      notification.success({
        message: CONFIRMATION_RECEIVING_CARGO,
        description: `${CHANGE_STATUS_DESCRIPTION}. Груз получен. По факту получено ${occupiedPlacesCountFact} ${declension(occupiedPlacesCountFact, PLACES_DECLENSION_DESCRIPTION)}`,
      });
      return true;
    } catch (e) {
      const err = (e as any).response?.data.message || SOMETHING_WRONG_TITLE;
      notification.error({
        message: 'Ошибка отправки данных',
        description: err,
      });
      return false;
    }
  }
}
