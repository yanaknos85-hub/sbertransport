import { EmployeeModel } from '@sber-sbertransport/mf-core';

import { CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';
import { UUID } from 'utils/io-ts';

import { KeyFilterStateMain, KeyFilterStateMainApproval } from '../../modules/CargosMultiple/CargoTabFilters/types';
import { TransportTypeEnum } from '../TransportTypes/TransportTypes.interface';
import { TripPaginationModel } from '../Trip/models/TripPagination.model';
import { CargoRegularRequestModel } from './models/CargoRegularRequest.model';
import { CargoRequestModel } from './models/CargoRequest.model';
import {
  CargoHistoryType,
  CargoRequestType,
  FilterSettingsType,
  KeyFilterState,
  RangePickerArg,
  Settings,
  SortProperty,
  TabSettings
} from './types';

export interface ICargosStore {
  cargoRequestList: CargoRequestModel[];
  cargoApprovalList: CargoRequestModel[];
  cargoRegularRequestList: CargoRequestModel[];
  cargoRegularApprovalList: CargoRequestModel[];
  pagination: TripPaginationModel;
  paginationRegular: TripPaginationModel;
  selfEmployee?: EmployeeModel;
  cargoRequest: CargoRequestModel | undefined;
  cargoRegularRequest: CargoRequestModel | undefined;
  cargoHistory: CargoHistoryType[];
  cargoRegularHistory: CargoHistoryType[];
  keyFilterState: KeyFilterState;
  keyFilterStateMain: KeyFilterStateMain;
  keyFilterStateMainApproval: KeyFilterStateMainApproval;
  activeTabFilter: Record<KeyFilterState, string>;
  tabFilterName: Record<KeyFilterState, string>;
  tabFilterNameMain: Record<KeyFilterState, string>;
  listPathName: string;
  sortingOrders: string;
  nameList: string;
  stateSorting: Record<string, string>;
  pageSetting: { page: number; size: number };
  desiredDateRange: RangePickerArg;
  checkedItem: string ;
  checkedListApproval: CargoRequestModel[];
  isCheckAll: boolean;
  isIndeterminate: boolean;
  settings: TabSettings;
  sortMapping: Record<SortProperty, { true: string; false: string }>;
  initialPageSetting: { page: number; size: number };
  initialSortSetting: { directionAsc: boolean; property: SortProperty; sortingOrders: string };
  initialTabFilterName: string;
  // Настройки фильтров в журнале заявок
  filterSettings: FilterSettingsType;
  setActiveSettings(nameList: string, activeTab: string, updatedSettings: Partial<Settings>);

  resetSettings(): void;
  setListPathName(pathName: string): void;
  resetListPathName(): void;
  setPageSetting(settings: { size: number; page: number }): void;
  resetSortingOrders(): void;
  setNameList(nameList: string): void;
  setSortingOrders(directionAsc: boolean, property: SortProperty): void;
  setActiveTabFilterMain(tab: string): void;
  setKeyFilterState(isRegular?: boolean): void;
  setTabFilterNameMain(name: string): void;
  setCheckedItem(value: string);
  setCheckedListApproval(list: CargoRequestModel[]);
  setIsCheckAll(value: boolean);
  setIsIndeterminate(value: boolean): void;
  setDesiredDateRange(dateRangeSetting: RangePickerArg);

  resetApprovalJournalFilters(nameList: string, activeTab: string): void;

  // Подтверждение получения груза
  confirmationReceivingCargo(rqUuid: string, occupiedPlacesCountFact: number, comment: string): Promise<boolean>;

  // Настройки фильтров в журнале заявок
  resetFilterSettings(): void;
  setFilterSettings(filterSettings: FilterSettingsType): void;
}

export interface ICargosService {
  getMultipleRequestListNonTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRequestListTerminal(data: ICargoRequestSearch): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRegularApproveListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRegularApproveListTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRegularRequestListTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRegularRequestListNonTerminal(
    data: ICargoRequestSearch
  ): Promise<TripPaginationModel & { content: CargoRequestType[] }>;
  getMultipleRequestById(rqUuid: UUID): Promise<CargoRequestModel>;
  getMultipleRegularRequestById(rqUuid: UUID): Promise<CargoRegularRequestModel>;
  getMultipleCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]>;
  getMultipleRegularCargoHistoryById(rqUuid: UUID): Promise<CargoHistoryType[]>;
  cancelMultipleRegularRequest(rqUuid: UUID, reason: string, code: number, field: string, value: string): Promise<void>;
  approveRequest(rqUuids: string[]): Promise<void>;
  declineRequest(rqUuids: string[], reason: string): Promise<void>;
  approveRegularRequest(tpUuids: string[]): Promise<void>;
  declineMultipleRegularRequest(tpUuids: string[], comment: string): Promise<void>;

  // Подтверждение получения груза
  confirmationReceivingCargo(rqUuid: string, occupiedPlacesCountFact: number, comment: string): Promise<void>;
}

export interface IFilters {
  authorId: string;
  senderId: string;
  recipientId: string;
}

export interface ICargoRequestSearch extends Partial<IFilters> {
  pageSetting?: { page: number; size: number };
  desiredDate?: { start?: string | null; end?: string | null };
  creationDate?: { start?: string | null; end?: string | null };
  shipmentTime?: { start?: string | null; end?: string | null };
  page?: { sort: { sorted: boolean } };
  sortSetting?: { directionAsc: boolean; property?: string };
  transportTypeEnum?: TransportTypeEnum[];
  requestStatusSet?: CargoRequestStatusesType[];
  requestHumanId?: string;
  regionTo?: string[];
  regionFrom?: string[];
  regionRoute?: string[];
  role?: string;
}
