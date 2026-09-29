import { Category, Tab } from 'modules/OrderExecution/constants/Tabs';
import { UUID } from 'utils/io-ts';
import {
  FeedSearchQuery,
  Order,
  OrderField,OrderFieldResult,
  SearchResponse,
  Source,
  TariffCost,
  TariffRequestMulti,
  OrderParams,
  CargoEngineerComment,
  SortDirection,
  AddDelegatePayload
} from '../Orders.types';
import { SchedulerSearchResponse } from '../SchedulerOrders.types';

/* TODO Перенесено из orderStore(он удален), далее удалить */
export interface IStatus<T> {
  name: T;
  rusName: string;
  editable: boolean;
  approvable: boolean;
  cancelable: boolean;
  finalStatus: boolean;
  color: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type TCarServiceStore = any;

export interface IOrganization {
  id: UUID;
  officialName: string;
}

export interface IDepartment {
  id: UUID;
  departmentName: string;
  parentId: UUID | null;
}

export interface IDepartmentListResponse {
  officialName: string;
  departmentDtoList: IDepartment[];
}

export interface ISearchEmployeeResponse {
  id: UUID;
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber: string;
}

export interface ICargoStore {
  cargoOrderList: any;
  cargoOrderActive: Order | undefined | null;
  clearOrderActive(): void;
  pageFilters: FeedSearchQuery;
  cargoQueryFilters: any;
  tariffsListMulti: TariffCost[];
  getCargoOrderList():Promise<void>;
  setCargoPageSetting(pagination: { page: number; size: number }): void;
  setCargoSchedulerPageSetting(pagination: { page: number; size: number }): void;
  setCargoFilterQueryProps(queryProps: any): void;
  resetFilters(): void;
  getCargoOrderActive(id: string, source: Source): void;
  changeOrder(orderId: string, orderFields: Partial<OrderParams>): Promise<void>;
  calculateAllTariffsMulti(data: TariffRequestMulti | undefined): Promise<void>;
  clearTariffs(): void;
  cargoSchedulerList: SchedulerSearchResponse | undefined;
  getCargoSchedulerList(): void;
  changeEngineerComment(orderId: string, query: string): Promise<void>;
  addAdditionalContact(humanReadableId: string, contactData: AddDelegatePayload[]): Promise<void>;
  sendOrderToContractor(orderId: string): Promise<void>;
  sendRouteToContractor(routeId: string): Promise<void>;
  setCargoSortOrder(field: string | null, direction: SortDirection): void;
  organizationId: UUID;
  activeTabKey: Tab;
  activeCategory: Category;
  setOrganizationId(organizationId: UUID): void;
  setActiveTabKey(activeKey: Tab): void;
  setActiveCategory(category: Category): void;
  initializeFilters(): void;
  // Методы для работы с OrderExecution (Feed) через useDeferredSearch
  setExecutorGroupId(executorGroupId: string[]): void;
  executorGroupId: string[];
  isOrganization: boolean;
  setIsOrganization(isOrganization: boolean): void;
  isLoadingOrg: boolean;
  isLoadingExec: boolean;
  // Методы выполнения поиска (используются вместе с useDeferredSearch)
  getCargoOrderListDeferredPost(emptyExecutorGroup?: boolean): void;
}

export interface ICargoService {
  getCargoOrderList: (orgId: UUID, query: FeedSearchQuery) => Promise<SearchResponse>;
  getCargoOrderActive: (orgId: UUID, source: Source) => Promise<Order>;
  changeOrder: (orderId: UUID, orderFields: OrderField[]) => Promise<OrderFieldResult[] | undefined>;
  calculateAllTariffsMulti(data: TariffRequestMulti | undefined): Promise<TariffCost[]>;
  getCargoSchedulerList: (orgId: UUID, query: FeedSearchQuery) => Promise<SchedulerSearchResponse>;
  changeEngineerComment: (humanReadableId: string, query: string) => Promise<CargoEngineerComment[] | void>;
  addAdditionalContact: (humanReadableId: string, contactData: AddDelegatePayload[]) => Promise<void>;
  sendOrderToContractor(orderId: string): Promise<any>;
  sendRouteToContractor(routeId: string): Promise<any>;
  getStateNumberList: (stateNumber: string) => Promise<string[] | void>;
  getEmployeeList: (name: string) => Promise<ISearchEmployeeResponse[] | void>;
  getEmployeeOrganization: () => Promise<IOrganization | void>;
  getOrganizationList: () => Promise<IOrganization[] | void>;
  getDepartmentList: (organizationIdList: UUID) => Promise<IDepartmentListResponse[] | void>;
  // Методы для работы с OrderExecution (Feed) через useDeferredSearch
  getFeedOrderListPost: (orgId: string | null | undefined, query: FeedSearchQuery, executorGroupIds?: string[], emptyExecutorGroup?: boolean) => Promise<SearchResponse>;
}

