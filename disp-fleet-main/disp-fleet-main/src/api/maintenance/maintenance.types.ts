import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import { createPagination, PaginationParams } from 'utils/io-ts/pagination';
import {
  MaintenanceTabs,
  WashingStatus,
  RepairStatus,
  TireStatus,
  EvacuationStatus,
  MaintenanceStatus,
  RegistrationStatus
} from 'api/maintenance/maintenance.constants';

type StatusByTab<T extends MaintenanceTabs> =
  T extends MaintenanceTabs.Washing ? WashingStatus :
    T extends MaintenanceTabs.Repair ? RepairStatus :
      T extends MaintenanceTabs.TireService ? TireStatus :
        T extends MaintenanceTabs.Evacuation ? EvacuationStatus :
          T extends MaintenanceTabs.Maintenance ? MaintenanceStatus :
            T extends MaintenanceTabs.Registration ? RegistrationStatus :
              string;

export interface MaintenanceFilters<T extends MaintenanceTabs = MaintenanceTabs> extends PaginationParams {
  fullName?: string;
  personnelNumber?: string;
  humanReadableId?: string;
  stateNumber?: string;
  organizationIdSet?: string[];
  departmentIdSet?: string[];
  requestStatusSet?: StatusByTab<T>[];
  creationTime?: { start: number; end: number };
  deadlineTime?: { start: number; end: number };
  type: T;
  contractorIdSet?: string[];
  autoparkIdSet?: string[];
}

export const MaintenanceMonitorItem = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  status: t.string,
  fullName: t.string,
  creationTime: t.number,
  deadlineTime: t.number,
  takeToWorkTime: t.number,
});
export type MaintenanceMonitorItem = t.TypeOf<typeof MaintenanceMonitorItem>;

export type MaintenanceMonitorItemTyped<T extends MaintenanceTabs> = Omit<MaintenanceMonitorItem, 'status'> & {
  status: StatusByTab<T>;
};

const MaintenanceMonitorResponse = createPagination(MaintenanceMonitorItem);
export type TMaintenanceMonitorResponse = t.TypeOf<typeof MaintenanceMonitorResponse>;

export type TMaintenanceMonitorResponseTyped<T extends MaintenanceTabs> = Omit<TMaintenanceMonitorResponse, 'content'> & {
  content: MaintenanceMonitorItemTyped<T>[];
};
