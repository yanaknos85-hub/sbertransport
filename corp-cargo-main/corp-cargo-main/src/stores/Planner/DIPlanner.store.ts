import type { ILogger } from '@sber-sbertransport/mf-core';
import { injectable, inject } from 'inversify';
import { action, observable } from 'mobx';
import { TYPES } from 'ioc/types';
import type {
  RouteType,
  DepartmentType,
  RoutesListStoreType,
  RouteResponseType,
  OrdersListMinimalStoreType,
  OrderListMinimalType
} from 'modules/Planner/types';

import {
  Employee,
  PaginationParams,
  EmployeeSearchQuery,
  Tab
} from 'modules/Planner/types';
import moment from 'moment';
import type { IPlannerService, IPlannerStore } from './Planner.interface';
import { mockedAuto } from 'modules/Planner/mockedData';
import { Statuses } from '../../modules/Planner/Components/Monitor/constants';
import type { MonitorFiltersType } from '../../modules/Planner/types';

const DEFAULT_PAGE_SIZE_VALUES = { page: 0, size: 10 };
const ORDER_INTERVAL = 20;

@injectable()
export class DIPlannerStore implements IPlannerStore {
  @inject(TYPES.IPlannerService)
  private service!: IPlannerService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    checkedOrdersListStore = [] as OrderListMinimalType[];

  @observable
    routeStore: RouteType | undefined;

  @observable
    draggedOrder: OrderListMinimalType | undefined;

  @observable
    totalParams = {
      weight: 0, volume: 0, cost: 0,
    };

  @observable
    ordersListStore: OrdersListMinimalStoreType = {
      content: [],
      totalElements: 0,
      totalPages: 0,
    };

  @observable
    routesListStore: RoutesListStoreType = {
      content: [], totalElements: 0, totalPages: 0,
    };

  @observable
    initialMonitorFilters: MonitorFiltersType = {
      regionTo: [],
      regionFrom: [],
      contractors: [],
      humanReadableId: undefined,
      desiredDateRange: {},
      creationDateRange: {},
      statusSet: Object.keys(Statuses).filter(
        s => s !== Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED && s !== Statuses.CARGO_CANCELED
      ),
    };

  @observable
    routesListStoreObj: RoutesListStoreType | undefined;

  @observable
    employeeListStore: Employee[] | undefined;

  @observable
    employeeListByOrg: Employee[] = [];

  @observable
    departmentsListByOrg: DepartmentType[] = [];

  @observable
    departmentsListStore: DepartmentType[] = [];

  @observable
    isDateInRange = false;

  @observable
    isDifferentDesiredDate = false;

  @observable
    isDifferentTariffType = false;

  @observable
    setRoutesListPageSize = DEFAULT_PAGE_SIZE_VALUES;

  @observable
    setOrdersListPageSize = DEFAULT_PAGE_SIZE_VALUES;

  @observable
    setMonitorListPageSize = DEFAULT_PAGE_SIZE_VALUES;

  @observable
    setEtrnListPageSetting = DEFAULT_PAGE_SIZE_VALUES;

  @observable
    monitorFilters!: MonitorFiltersType;

  @observable
    employeeId = '';

  @observable
    activeTab = Tab.planner;

  constructor() {
    this.setRoutesListPageSize = DEFAULT_PAGE_SIZE_VALUES;
    this.setOrdersListPageSize = DEFAULT_PAGE_SIZE_VALUES;
    this.setMonitorListPageSize = DEFAULT_PAGE_SIZE_VALUES;
    this.setEtrnListPageSetting = DEFAULT_PAGE_SIZE_VALUES;
    this.monitorFilters = {
      humanReadableId: undefined,
      regionFrom: [],
      regionTo: [],
      creationDateRange: {},
      desiredDateRange: {},
      autoId: undefined,
      authorEmployeeId: undefined,
      departmentId: undefined,
      organizationId: undefined,
      contractors: [],
      statusSet: Object.keys(Statuses).filter(
        s => s !== Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED && s !== Statuses.CARGO_CANCELED
      ),
    };
    this.employeeId = '';
  }

  @action.bound
  setEmployeeId(id: string) {
    this.employeeId = id;
  }

  @action.bound
    setMonitorFilters = (filters: MonitorFiltersType) => {
      this.monitorFilters = filters;
    };

  @action.bound
    setActiveTabKey = (activeTab: Tab): void => {
      this.activeTab = activeTab;
    };

  @action.bound
  saveOrdersToStore(orders: OrdersListMinimalStoreType) {
    this.ordersListStore = orders;
  }

  @action.bound
  saveRoutesToStore(routes: RouteResponseType) {
    this.routesListStore = routes;
  }

  @action.bound
  addOrderToRoute(order: OrderListMinimalType) {
    this.checkDate(order.desiredDate);
    this.checkTariffType(order.tariffId);
    if (this.checkedOrdersListStore.length > 0 && this.isDifferentDesiredDate) {
      this.logger.toNotify(
        'error',
        `Объединяемые заявки должны входить в интервал ${ORDER_INTERVAL} дней. Заявка не добавлена в маршрут`,
        'Проверьте интервал дат',
        5,
        667
      );
    } else {
      this.checkedOrdersListStore.push(order);
    }
  }

  @action.bound
  private checkDate(desiredDate: string): void {
    if (this.checkedOrdersListStore.length) {
      const allDates = [...this.checkedOrdersListStore.map(order => order.desiredDate), desiredDate];
      const minDate = moment.min(allDates.map(date => moment(date)));
      const maxDate = moment.max(allDates.map(date => moment(date)));
      this.isDifferentDesiredDate = maxDate.diff(minDate, 'days') > ORDER_INTERVAL;
    }
  }

  @action.bound
  private checkTariffType(tariffId: string): void {
    if (this.checkedOrdersListStore.length) {
      this.isDifferentTariffType = this.checkedOrdersListStore.every(
        item => item.tariffId !== tariffId
      );
    }
  }

  @action.bound
  removeOrderRoute(id: string) {
    this.checkedOrdersListStore = this.checkedOrdersListStore.filter(
      order => order.id !== id
    );
    if (this.checkedOrdersListStore.length > 1) {
      for (let i = 0; i < this.checkedOrdersListStore.length - 1; i++) {
        this.isDateInRange = moment(
          this.checkedOrdersListStore[i].desiredDate
        ).format('LL') !== moment(this.checkedOrdersListStore[i + 1].desiredDate).format('LL');
      }
    } else {
      this.isDateInRange = false;
    }
  }

  @action.bound
  dragOrder(orderId: string, list: OrderListMinimalType[]) {
    this.draggedOrder = list?.find(order => order.id === orderId);
  }

  @action.bound
  handleResetOrder() {
    this.draggedOrder = undefined;
  }

  @action.bound
  clearCheckedListStore() {
    this.checkedOrdersListStore = [];
  }

  @action.bound
  private updateRouteStore(route: RouteType) {
    this.routeStore = route;
  }

  @action.bound
  clearRouteStore() {
    this.routeStore = undefined;
  }

  @action.bound
  clearStores() {
    this.clearRouteStore();
    this.clearCheckedListStore();
  }

  @action.bound
  dragOrderToRoute(
    routeId: string,
    list: RouteType[],
    draggedOrder?: OrderListMinimalType,
    rawCheckedList?: OrderListMinimalType[]
  ) {
    const route = list.find(r => r.id === routeId) as RouteType;
    const updatedRouteObj = { ...route, auto: mockedAuto } as RouteType;
    const checkedList = !!draggedOrder && !rawCheckedList?.length ? [draggedOrder] : rawCheckedList;
    if (checkedList?.length) {
      const newDesiredDate = moment.max(checkedList.map(route => moment(route.desiredDate)));
      if (newDesiredDate.isAfter(moment(route.desiredDate))) {
        updatedRouteObj.desiredDate = newDesiredDate.format('YYYY-MM-DD HH:mm:ss');
      }
      const allRouteDates = route.waypoints.flatMap(waypoint => {
        return waypoint.requests.map(item => item.request.desiredDate);
      });
      const allDates = [...checkedList.map(order => order.desiredDate), ...allRouteDates];
      const minDate = moment.min(allDates.map(date => moment(date)));
      const maxDate = moment.max(allDates.map(date => moment(date)));
      const diffDays = maxDate.diff(minDate, 'days');
      this.isDateInRange = diffDays <= ORDER_INTERVAL;
      if (this.isDateInRange) {
        const newRoute = { ...updatedRouteObj, addRequests: checkedList.map(o => o.id) } as RouteType;
        this.updateRouteStore(newRoute);
        return newRoute;
      } else {
        this.logger.toNotify(
          'error',
          `Объединяемые заявки должны входить в интервал ${ORDER_INTERVAL} дней. Заявка не добавлена в маршрут.`,
          'Проверьте интервал дат',
          5,
          667
        );
      }
    }
    return null;
  }

  @action.bound
  handleChooseRoute(routeId: string, list: RouteType[]) {
    this.dragOrderToRoute(routeId, list, undefined, this.checkedOrdersListStore);
  }

  @action.bound
  async getAllEmployeesByOrganization(
    organizationId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<void> {
    const data = await this.service.getAllEmployeesByOrganization(
      organizationId,
      query,
      pagination
    );
    this.employeeListByOrg = data.content ?? [];
  }

  @action.bound
  async getAllDepartmentsByOrganization(
    organizationId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<void> {
    const data = await this.service.getAllDepartmentsByOrganization(
      organizationId,
      query,
      pagination
    );
    this.departmentsListByOrg = data.content ?? [];
  }

  @action.bound
    setRoutesPageSettings = (
      { page, size }: { page: number; size: number }
    ): void => {
      this.setRoutesListPageSize.page = page;
      this.setRoutesListPageSize.size = size;
    };

  @action.bound
    setMonitorPageSettings = (
      { page, size }: { page: number; size: number }
    ): void => {
      this.setMonitorListPageSize.page = page;
      this.setMonitorListPageSize.size = size;
    };

  @action.bound
    setEtrnPageSettings = (
      { page, size }: { page: number; size: number }
    ): void => {
      this.setEtrnListPageSetting.page = page;
      this.setEtrnListPageSetting.size = size;
    };

  @action.bound
    setOrdersPageSettings = (
      { page, size }: { page: number; size: number }
    ): void => {
      this.setOrdersListPageSize.page = page;
      this.setOrdersListPageSize.size = size;
    };

  @action.bound
    setCargoPageSetting = (
      { page, size }: { page: number; size: number }
    ): void => {
      this.setOrdersListPageSize.page = page;
      this.setOrdersListPageSize.size = size;
    };

  @action.bound
  setRoutePage(page: number) {
    this.setRoutesListPageSize.page = page;
  }

  @action.bound
  setOrderPage(page: number) {
    this.setOrdersListPageSize.page = page;
  }

  @action.bound
  setMonitorPage(page: number) {
    this.setMonitorListPageSize.page = page;
  }
}
