import type { UUID } from 'utils/io-ts';
import type { Statuses as RepairStatuses } from 'modules/OrderExecution/constants/CarService/Repair';
import type { Statuses as MaintenanceStatuses } from 'modules/OrderExecution/constants/CarService/Maintenance';

interface Count {
  number: number;
}

export interface WorkOrderItem<T> {
  value: T;
  comment: string;
}

export interface Vehicle {
  [key: string]: string | WorkOrderItem<string>;
  brand: string;
  mileage: string;
  model: string;
  stateNumber: WorkOrderItem<string>;
  vin: string;
  year: string;
}

export interface Detail {
  [key: string]: string | number | WorkOrderItem<number> | WorkOrderItem<string>;
  amount: number;
  nameNormalized: WorkOrderItem<string>;
  ordinal: number; // Порядковый номер поля. Если в будущем нарушиться очерёдность - сортировать по нему.
  totalPrice: WorkOrderItem<number>;
  unit: string;
  unitPrice: number;
}

export interface Work {
  [key: string]: string | number | WorkOrderItem<number> | WorkOrderItem<string>;
  amount: number;
  hourNormalized: WorkOrderItem<number>;
  hourPrice: number;
  nameNormalized: WorkOrderItem<string>;
  ordinal: number; // Порядковый номер поля. Если в будущем нарушиться очерёдность - сортировать по нему.
  totalPrice: WorkOrderItem<number>;
}

export interface IWorkOrder {
  details: Detail[];
  haveComments?: boolean;
  name?: string;
  number: string;
  s3Id?: string;
  totalDetailPrice: WorkOrderItem<number>;
  totalPrice: WorkOrderItem<number>;
  totalWorkPrice: WorkOrderItem<number>;
  vehicle: Vehicle;
  works: Work[];
}

export interface VehicleTableData extends Count {
  category: string;
  errorComment: string | null;
  info: string | number;
}

export interface WorkTableData extends Count {
  amount: number;
  hourNormalized: number;
  hourNormalizedComment: string | null;
  hourPrice: number;
  nameNormalized: string;
  nameNormalizedComment: string | null;
  totalPrice: number;
  totalPriceComment: string | null;
  hasError: boolean;
}

export interface DetailTableData extends Count {
  amount: number;
  nameNormalized: string;
  nameNormalizedComment: string | null;
  totalPrice: number;
  totalPriceComment: string | null;
  unit: string;
  unitPrice: number;
  hasError: boolean;
}

export interface TotalTableData extends Count {
  errorComment: string | null;
  nameNormalized: string;
  unitPrice: number;
}

export interface NewOrderDto {
  file: string | ArrayBuffer | null;
  orderDto: IWorkOrder;
}
export interface WorkOrderColumn {
  title: string;
  dataIndex: string;
  key: string;
  width?: number;
}

export interface IGetWorkOrderResponse {
  data: ArrayBuffer;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  headers: any;
}

export interface IWorkOrderParamsRequest {
  stateNumber: string;
  organizationId: UUID;
  humanReadableId: string;
}

export interface SaveChangeOrderQuery {
  status?: RepairStatuses | MaintenanceStatuses;
  deadlineTime?: number;
  newOrderDto?: NewOrderDto;
}
