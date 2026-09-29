import { injectable } from 'inversify';
import type { IApprovalsStore, FiltersSettingsType } from './Approvals.interface';
import { action, observable } from 'mobx';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

const initialPaginationSettings = {
  page: 0,
  size: 5,
};

const initialFiltersSettings = {
  transportType: undefined,
  statusSettings: '',
} as FiltersSettingsType;

@injectable()
export class DIApprovalsStore implements IApprovalsStore {
  @observable
    paginationSettings = initialPaginationSettings;

  @observable
    filtersSettings = initialFiltersSettings;

  @observable
    isFromDetailedPage = false;

  @observable
    filter = 'active';

  @action.bound
  setPagination(page: number, size?: number) {
    this.paginationSettings.page = page - 1;
    this.paginationSettings.size = size || 5;
  }

  @action.bound
  resetPagination() {
    this.paginationSettings.page = initialPaginationSettings.page;
    this.paginationSettings.size = initialPaginationSettings.size;
  }

  @action.bound
  setDetailedPageStatus(value: boolean) {
    this.isFromDetailedPage = value;
  }

  @action.bound
  resetFilters() {
    this.filtersSettings = initialFiltersSettings;
  }

  @action.bound
  setFilter(value: string) {
    this.filter = value;
  }

  @action.bound
  setFiltersTransportType(value: TransportTypeEnum | undefined) {
    this.filtersSettings.transportType = value;
  }

  @action.bound
  setFiltersStatusSettings(value: string) {
    this.filtersSettings.statusSettings = value;
  }
}
