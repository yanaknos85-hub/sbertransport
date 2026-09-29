import {
  OrderListMinimalType,
  RouteType,
  RoutesListStoreType,
  OrdersListMinimalStoreType,
  Tab
} from 'modules/Planner/types';
import { Employee, PaginationParams } from '../Employee/Employee.interface';
import { EmployeeSearchQuery } from '../../api/employee/search';
import { DepartmentType } from 'modules/Planner/types';
import type { MonitorFiltersType } from '../../modules/Planner/types';

export interface IPlannerStore {
  checkedOrdersListStore: OrderListMinimalType[];

  routeStore: RouteType | undefined;
  draggedOrder: OrderListMinimalType | undefined;
  totalParams: { weight: number; volume: number; cost: number };

  ordersListStore: OrdersListMinimalStoreType;

  routesListStore: RoutesListStoreType;
  employeeListStore: Employee[] | undefined;
  employeeListByOrg: Employee[];
  departmentsListByOrg: DepartmentType[];
  departmentsListStore: DepartmentType[];
  isDateInRange: boolean;
  isDifferentDesiredDate: boolean;
  isDifferentTariffType: boolean;
  setRoutesListPageSize: { page: number; size: number };
  setOrdersListPageSize: { page: number; size: number };
  setMonitorListPageSize: { page: number; size: number };
  setEtrnListPageSetting: { page: number; size: number };
  monitorFilters: MonitorFiltersType;
  initialMonitorFilters: MonitorFiltersType;
  activeTab: Tab;
  employeeId: string;

  setEmployeeId(id: string): void;
  setMonitorFilters(filters: MonitorFiltersType): void;
  setActiveTabKey(activeTab: Tab): void;
  setCargoPageSetting(pagination: { page: number; size: number }): void;
  setRoutePage(page: number): void;
  setOrderPage(page: number): void;
  setMonitorPage(page: number): void;
  saveOrdersToStore(orders: OrdersListMinimalStoreType): void;
  saveRoutesToStore(routes: RoutesListStoreType): void;
  addOrderToRoute(order: OrderListMinimalType): void;
  removeOrderRoute(id: string): void;
  dragOrder(orderId: string, list: OrderListMinimalType[]): void;
  handleResetOrder(): void;
  clearCheckedListStore(): void;
  clearRouteStore(): void;
  clearStores(): void;
  dragOrderToRoute(
    routeId: string,
    list: RouteType[],
    draggedOrder?: OrderListMinimalType,
    checkedList?: OrderListMinimalType[]
  ): RouteType | null;
  handleChooseRoute(routeId: string, list: RouteType[]): void;
  setRoutesPageSettings({ page, size }: { page: number; size: number }): void;

  setOrdersPageSettings({ page, size }: { page: number; size: number }): void;

  setMonitorPageSettings({ page, size }: { page: number; size: number }): void;

  setEtrnPageSettings({ page, size }: { page: number; size: number }): void;

  getAllEmployeesByOrganization(
    organizationId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<void>;

  getAllDepartmentsByOrganization(
    orgId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<void>;
}

export interface IPlannerService {
  getAllEmployeesByOrganization(
    orgId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<{ content: Employee[] }>;

  getAllDepartmentsByOrganization(
    orgId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<{ content: DepartmentType[] }>;
}
