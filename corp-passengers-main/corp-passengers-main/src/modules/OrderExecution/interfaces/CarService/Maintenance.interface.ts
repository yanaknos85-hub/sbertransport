import type { AxiosRequestConfig } from 'axios';
import type { UUID } from 'utils/io-ts';
import type { TransportType } from '../../constants/General';
import type { ServiceType } from '../../constants/CarService';
import type { Statuses } from '../../constants/CarService/Maintenance';
import type {
  IPersonInfo, IEvaluationInfo, ICarInfo, IOrderListResponse
} from './General';
import type { IWorkOrder, IWorkOrderParamsRequest, SaveChangeOrderQuery } from './WorkOrder.interface';
import type { IFilter } from '../Filters.interface';

export interface IMaintenanceOrder {
  id: UUID;
  humanReadableId: string;
  transportType: TransportType;
  status: Statuses;
  takeToWorkTime?: number;
  creationTime: number;
  transferToRepairTime: number;
  deadlineTime?: number;
  finishedTime?: number;
  serviceType: ServiceType;
  comment?: string;
  author: IPersonInfo;
  colleague: IPersonInfo;
  vehicle: ICarInfo;
  evaluation?: IEvaluationInfo;
  order?: IWorkOrder;
  orderId: UUID;
}

export interface TMaintenanceOrderQuery {
  status: Statuses;
}

export type TMaintenanceOrderListResponse = IOrderListResponse<IMaintenanceOrder[]>;

export interface IMaintenanceService {
  getOrderList: (filters: IFilter) => Promise<TMaintenanceOrderListResponse | void>;
  getOrder: (orderId: UUID) => Promise<IMaintenanceOrder | void>;
  takeToWork: (orderId: UUID) => Promise<void>;
  editOrder: (orderId: UUID, query: SaveChangeOrderQuery) => Promise<void>;
  getWorkOrder: (orderId: UUID) => Promise<AxiosRequestConfig | void>;
  checkWorkOrder: (formData: FormData, params: IWorkOrderParamsRequest, token: string) => Promise<IWorkOrder | void>;
  saveChangeOrder: (orderId: UUID, query: SaveChangeOrderQuery) => Promise<void>;
  deleteWorkOrder: (orderId: UUID) => Promise<void>;
  savePriceForEconomy: (orderId: UUID, totalPrice: number | null) => Promise<void>;
}

export interface IMaintenanceStore {
  orderListConfig: TMaintenanceOrderListResponse | null;
  order: IMaintenanceOrder | null;
  workOrder: IWorkOrder | null;
  workOrderFile: File | null;
  workOrderSaved: boolean;
  filter: IFilter;
  getOrder(orderId: UUID): void;
  getOrderList(): Promise<boolean>;
  setRefreshInterval(): void;
  clearRefreshInterval(): void;
  clearOrder(): void;
  takeToWork(orderId: UUID): Promise<void>;
  setStatus(orderId: UUID, status: Statuses): void;
  setWorkOrder(): Promise<void>;
  setTotalWorkOrderDetailPrice(price: number): void;
  setTotalWorkOrderWorkPrice(price: number): void;
  removeWorkOrder(): void;
  downloadWorkOrder(orderID: UUID, fileName: string): void;
  checkWorkOrder(FormData: FormData, token: string): Promise<void>;
  clearWorkOrder(): void;
  savePriceForEconomy(totalPrice: number | null): void;
  setFilter(newFilter: IFilter, isReset?: boolean): void;
}
