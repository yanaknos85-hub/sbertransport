import type { UUID } from 'utils/io-ts';
import type { TransportType } from 'modules/OrderExecution/constants/General';
import type { ServiceType } from 'modules/OrderExecution/constants/CarService';
import type { Statuses, TireTypes, Objectives } from 'modules/OrderExecution/constants/CarService/Tire';
import type {
  IAddressInfo, IPersonInfo, IEvaluationInfo, ICarInfo, IOrderListResponse
} from './General';
import type { IFilter } from '../Filters.interface';

export interface ITireType {
  name: TireTypes;
  objectives: Objectives[];
}

export interface ITireOrder {
  address: IAddressInfo;
  author: IPersonInfo;
  colleague?: IPersonInfo;
  comment?: string;
  creationTime: number;
  deadlineTime: number;
  evaluation?: IEvaluationInfo;
  finishedTime?: number;
  humanReadableId: string;
  id: UUID;
  serviceType: ServiceType;
  status: Statuses;
  takeToWorkTime?: number;
  transferTime?: number;
  transportType: TransportType;
  type: ITireType;
  vehicle: ICarInfo;
}

export interface ITireEditOrderQuery {
  status: Statuses;
}

export type TTireOrderListResponse = IOrderListResponse<ITireOrder[]>;

export interface ITireService {
  getOrderList: (filters: IFilter) => Promise<TTireOrderListResponse | void>;
  getOrder: (orderId: UUID) => Promise<ITireOrder | void>;
  takeToWork: (orderId: UUID) => Promise<void>;
  editOrder: (orderId: UUID, query: ITireEditOrderQuery) => Promise<void>;
}

export interface ITireStore {
  orderListConfig: TTireOrderListResponse | null;
  order: ITireOrder | null;
  filter: IFilter;
  getOrder(orderId: UUID): void;
  getOrderList(): Promise<boolean>;
  setRefreshInterval(): void;
  clearRefreshInterval(): void;
  clearOrder(): void;
  takeToWork(orderId: UUID): Promise<void>;
  setStatus(orderId: UUID, status: Statuses): void;
  setFilter(newFilter: IFilter, isReset?: boolean): void;
}
