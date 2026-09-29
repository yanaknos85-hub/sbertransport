import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

export interface FiltersSettingsType {
  transportType: TransportTypeEnum | undefined;
  statusSettings: string;
}

export interface IApprovalsStore {
  filter: string;

  paginationSettings: {
    page: number;
    size: number;
  };

  filtersSettings: FiltersSettingsType;

  isFromDetailedPage: boolean;

  setPagination(page: number, size?: number): void;
  resetPagination(): void;
  setDetailedPageStatus(value: boolean): void;
  resetFilters(): void;
  setFilter(value: string): void;
}
