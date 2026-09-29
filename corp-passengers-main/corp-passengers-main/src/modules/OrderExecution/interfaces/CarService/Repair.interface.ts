import type { AxiosRequestConfig } from 'axios';
import type { Moment } from 'moment';

import type { UUID } from 'utils/io-ts';
import type { ServiceType } from '../../constants/CarService';
import type { Defects, Problems, Statuses } from '../../constants/CarService/Repair';
import type {
  IAddressInfo, IApprovedInfo, IPersonInfo, IOrderListResponse
} from './General';
import type {
  IWorkOrder, IWorkOrderParamsRequest, SaveChangeOrderQuery, Vehicle
} from './WorkOrder.interface';
import type { IFilter } from '../Filters.interface';

export interface IProblemInfo {
  problemName: Problems;
  defects: Defects[];
}

// TODO Сравнить с заявками из списка и по необходимости сделать отдельный интерфейс
export interface IRepairOrder {
  address: IAddressInfo;
  approvalDate?: string;
  approvalState: string; // AWAITING_APPROVAL
  approvedBy?: IApprovedInfo;
  author: IPersonInfo;
  id: UUID;
  haveWorkOrder?: boolean;
  humanReadableId: string;
  comment: string;
  transportType: string; // TAXI
  tariffId: UUID;
  pickupTime: number;
  deadlineTime?: number;
  deadlineTimeChange: number;
  deadlineTimeChanger: IPersonInfo;
  status: Statuses;
  creationTime: number;
  vehicle: Vehicle;
  problems: IProblemInfo[];
  expediency: string;
  finishedTime: number;
  takeToWorkTime: number;
  order?: IWorkOrder;
  orderId: UUID;
  serviceType: ServiceType;
}

export type TRepairOrderListResponse = IOrderListResponse<IRepairOrder[]>;

export interface IRepairService {
  getOrderList: (filters: IFilter) => Promise<TRepairOrderListResponse | void>;
  getOrder: (orderId: UUID) => Promise<IRepairOrder | void>;
  takeToWork: (orderId: UUID) => Promise<void>;
  getWorkOrder: (orderId: UUID) => Promise<AxiosRequestConfig | void>;
  checkWorkOrder: (formData: FormData, params: IWorkOrderParamsRequest, token: string) => Promise<IWorkOrder | void>;
  getDeadline: (orderId: UUID) => Promise<number | void>;
  saveChangeOrder: (orderId: UUID, query: SaveChangeOrderQuery) => Promise<void>;
  deleteWorkOrder: (orderId: UUID) => Promise<void>;
  savePriceForEconomy: (orderId: UUID, totalPrice: number | null) => Promise<void>;
}

export interface IRepairStore {
  orderListConfig: TRepairOrderListResponse | null;
  order: IRepairOrder | null;
  workOrder: IWorkOrder | null;
  workOrderSaved: boolean;
  workOrderFile: File | null;
  filter: IFilter;
  getOrderList(): Promise<boolean>;
  setRefreshInterval(): void;
  clearRefreshInterval(): void;
  getOrder(orderId: UUID): void;
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
  clearOrder(): void;
  getDeadline(orderID: UUID): Promise<Moment | undefined>;
  setDeadLine(orderID: UUID, deadLine: number): void;
  setFilter(newFilter: IFilter, isReset?: boolean): void;
}
