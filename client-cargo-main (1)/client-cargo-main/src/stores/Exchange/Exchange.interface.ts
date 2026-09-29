import { ExchangeResponse } from '../../modules/Exchange/types';
import { PaginationModel } from './models/Pagination.model';
import { SortProperty } from './types';

export interface IExchangeStore {
  pagination: PaginationModel;
  searchId: string | null;
  sortingOrders: string;
  stateSorting: Record<string, string>;
  pageSetting: { page: number; size: number };
  sortSetting: { directionAsc: boolean; property: SortProperty };
  exchangeRequest: ExchangeResponse | {};

  updateRouteStatusExchange(id: string, status: string, type: string): Promise<void>;
  getRequestById(
    type: string,
    id: string
  ): Promise<void>;
  getFilterAvailableList(
    type: string,
    addressFrom: string,
    addressTo: string,
    desiredDateRange?: { start: string; end: string }): Promise<void>;
  takeToWorkExchange(id: string, type: string): Promise<void>;
  denyExchange(id: string, type: string): Promise<void>;
  getAvailableList(
    type: string,
    pageSetting: { page: number; size: number },
    sortSetting: { directionAsc: boolean; property: SortProperty }): void;
  getAvailableListWithFilters(
    type: string,
    pageSetting: { page: number; size: number },
    sortSetting: { directionAsc: boolean; property: SortProperty },
    addressFrom?: string,
    addressTo?: string,
    desiredDateRange?: { start: string; end: string }
  ): Promise<void>;
  setPageSetting(settings: { size: number; page: number }): void;
  setSortSetting(directionAsc: boolean, property: SortProperty): void;
  resetSettings(): void;
  resetSortingOrders(): void;
  setSortingOrders(directionAsc: boolean, property: SortProperty): void;
}
