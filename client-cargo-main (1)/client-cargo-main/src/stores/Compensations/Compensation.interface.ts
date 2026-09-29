import * as t from 'io-ts';

import { CompensationRequestModel } from './models/CargoRequest.model';
import { CompensationPaginationModel } from './models/CompensationPagination.model';
import { ICompensationRequestSearch, Settings, SortProperty } from './types';

export enum StatusLableName {
  ACTIVE = 'active',
  FINAL = 'final',
}

export interface RequestIds {
  requestIds: string[];
}

export interface DeclineData extends RequestIds {
  reason: string;
}

export const IODeclineReason = t.type({
  reason: t.string,
});

export type IDeclineReason = t.TypeOf<typeof IODeclineReason>;

export interface ICompensationService {
  getCompensationListTerminal(data: ICompensationRequestSearch): Promise<CompensationPaginationModel & { content: CompensationRequestModel[] }>;
  getCompensationListNonTerminal(data: ICompensationRequestSearch): Promise<CompensationPaginationModel & { content: CompensationRequestModel[] }>;
  approveRequest(requestIds: RequestIds): Promise<void>;
  declineRequest(declineData: DeclineData): Promise<void>;
}

export interface ICompensationStore {
  compensationList: CompensationPaginationModel & { content: CompensationRequestModel[] };
  paginationCompensation: CompensationPaginationModel;
  loading: boolean;
  pageSetting: { page: number; size: number };
  checkedListApproval: CompensationRequestModel[];
  nameList: string;
  settings: {
    compensationList: {
      active: Settings;
      final: Settings;
    };
  };
  isCheckAll: boolean;
  isIndeterminate: boolean;
  sortMapping: Record<SortProperty, { true: string; false: string }>;

  setCheckedItem(value: string);
  setIsCheckAll(value: boolean): void;
  setIsIndeterminate(value: boolean): void;
  getCompensationListTerminal(data: ICompensationRequestSearch): Promise<void>;
  getCompensationListNonTerminal(data: ICompensationRequestSearch): Promise<void>;
  approveRequest(requestIds: RequestIds): Promise<boolean>;
  declineRequest(declineData: DeclineData): Promise<boolean>;
  setCheckedListApproval(list: CompensationRequestModel[]): void;
  setNameList(nameList: string): void;
  setActiveSettings(nameList: string, activeTab: string, updatedSettings: Partial<Settings>): void;
  setPageSetting(settings: { page: number; size: number }): void;
  resetApprovalJournalFilters(nameList: string, activeTab: string): void;
}
