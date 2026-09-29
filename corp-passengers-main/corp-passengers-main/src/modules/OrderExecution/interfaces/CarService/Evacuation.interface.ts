import type { UUID } from 'utils/io-ts';
import { TransportType } from '../../constants/General';
import { Statuses, Types } from '../../constants/CarService/Evacuation';
import type {
  IAddressInfo, IPersonInfo, IEvaluationInfo, ICarInfo, IOrderListResponse
} from './General';
import type { IFilter } from '../Filters.interface';

// TODO Сравнить с заявками из списка и по необходимости сделать отдельный интерфейс
export interface IEvacuationOrder {
  addressFrom: IAddressInfo;
  addressTo: IAddressInfo;
  author: IPersonInfo;
  colleague?: IPersonInfo;
  comment?: string;
  creationTime: number;
  deadlineTime: number;
  evaluation?: IEvaluationInfo;
  finishedTime?: number;
  humanReadableId: string;
  id: UUID;
  pickupTime: number;
  status: Statuses;
  takeToWorkTime?: number;
  transportType: TransportType;
  type: Types;
  vehicle: ICarInfo;
}

export interface IEvacuationEditOrderQuery {
  status?: Statuses;
  deadlineTime?: number;
}

export type TEvacuationOrderListResponse = IOrderListResponse<IEvacuationOrder[]>;

export interface IEvacuationService {
  getOrderList: (filters: IFilter) => Promise<TEvacuationOrderListResponse | void>;
  getOrder: (orderId: UUID) => Promise<IEvacuationOrder | void>;
  takeToWork: (orderId: UUID) => Promise<void>;
  editOrder: (orderId: UUID, query: IEvacuationEditOrderQuery) => Promise<void>;
}

export interface IEvacuationStore {
  orderListConfig: TEvacuationOrderListResponse | null;
  order: IEvacuationOrder | null;
  filter: IFilter;
  getOrderList(): Promise<boolean>;
  setRefreshInterval(): void;
  clearRefreshInterval(): void;
  getOrder(orderId: UUID): void;
  clearOrder(): void;
  takeToWork(orderId: UUID): Promise<void>;
  setStatus(orderId: UUID, status: Statuses): void;
  setFilter(newFilter: IFilter, isReset?: boolean): void;
}
