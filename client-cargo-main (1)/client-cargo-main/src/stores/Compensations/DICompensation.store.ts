import type { ILogger } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, observable } from 'mobx';
import { CHANGE_STATUS_DESCRIPTION, SOMETHING_WRONG_TITLE } from 'shared/constants/constants';
import { plainToNew } from 'utils';

import type { DeclineData, ICompensationStore, RequestIds } from './Compensation.interface';
import type { ICompensationService } from './Compensation.interface';
import { CompensationRequestModel } from './models/CargoRequest.model';
import { CompensationPaginationModel } from './models/CompensationPagination.model';
import {
  Filters,
  ICompensationRequestSearch, Settings, SortProperty
} from './types';

@injectable()
export class DICompensationStore implements ICompensationStore {
  @inject(TYPES.ICompensationService)
  private service!: ICompensationService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    initialPageSetting: { page: number; size: number } = {
      page: 0,
      size: 10,
    };

  @observable
    isIndeterminate = false;

  @observable
    pageSetting: { page: number; size: number } = this.initialPageSetting;

  @observable
    paginationCompensation: CompensationPaginationModel = {
      size: 10,
      totalElements: -1,
      totalPages: 0,
      number: 0,
    };

  @observable
    checkedListApproval: CompensationRequestModel[] = [];

  @observable
    isCheckAll = false;

  @observable
    nameList = '';

  @observable
    settings = {
      compensationList: {
        active: { ...this.createSettings(), tabFilterName: Filters.all },
        final: { ...this.createSettings(), tabFilterName: Filters.all },
      },
    };

  @observable
    checkedItem = '';

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

  compensationList: CompensationPaginationModel & { content: CompensationRequestModel[] } = {
    content: [],
    number: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
  };

  @observable
    loading = false;

  createSettings(): Settings {
    return {
      sortSetting: {
        directionAsc: false,
        property: SortProperty.CREATION_DATE,
        sortingOrders: 'По времени создания: По убыванию',
      },
    };
  }

  @action.bound
  setIsIndeterminate(value: boolean): void {
    this.isIndeterminate = value;
  }

  @action.bound
  setNameList(nameList: string): void {
    this.nameList = nameList;
  }

  @action.bound
  setCheckedListApproval(list: CompensationRequestModel[]) {
    this.checkedListApproval = list;
  }

  @action.bound
  setIsCheckAll(value: boolean): void {
    this.isCheckAll = value;
  }

  @action.bound
  setCheckedItem(value: string): void {
    this.checkedItem = value;
  }

  @action.bound
  setActiveSettings(nameList: string, activeTab: string, updatedSettings: Partial<Settings>) {
    this.settings[nameList][activeTab] = {
      ...this.settings[nameList][activeTab],
      ...updatedSettings,
    };
  }

  @action.bound
  setPageSetting(settings: { page: number; size: number }): void {
    this.pageSetting = { ...settings };
  }

  @action.bound
  resetApprovalJournalFilters(nameList: string, activeTab: string): void {
    this.setPageSetting({ ...this.initialPageSetting });
    this.setActiveSettings(nameList, activeTab, {
      tabFilterName: Filters.all,
      sortSetting: {
        directionAsc: false,
        property: SortProperty.CREATION_DATE,
        sortingOrders: 'По времени создания: По убыванию',
      },
    });
  }

  // Остальные методы без изменений...
  async getCompensationListTerminal(data: ICompensationRequestSearch): Promise<void> {
    this.loading = true;
    try {
      const result = await this.service.getCompensationListTerminal(data);
      this.compensationList = result;
      this.paginationCompensation = plainToNew(CompensationPaginationModel, result);
    } catch (error) {
      this.compensationList = { ...this.compensationList, content: [] };
    } finally {
      this.loading = false;
    }
  }

  async getCompensationListNonTerminal(data: ICompensationRequestSearch): Promise<void> {
    this.loading = true;
    try {
      const result = await this.service.getCompensationListNonTerminal(data);
      this.compensationList = result;
      this.paginationCompensation = plainToNew(CompensationPaginationModel, result);
    } catch (error) {
      this.compensationList = { ...this.compensationList, content: [] };
    } finally {
      this.loading = false;
    }
  }

  @action.bound
  async approveRequest(requestIds: RequestIds): Promise<boolean> {
    try {
      await this.service.approveRequest(requestIds);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (error) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }

  @action.bound
  async declineRequest(declineData: DeclineData): Promise<boolean> {
    try {
      await this.service.declineRequest(declineData);
      this.logger.toMessage('success', CHANGE_STATUS_DESCRIPTION);
      return true;
    } catch (error) {
      this.logger.toMessage('error', SOMETHING_WRONG_TITLE);
      return false;
    }
  }
}
