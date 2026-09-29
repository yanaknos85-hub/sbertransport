import type { UUID } from 'utils/io-ts';

export interface IDateRange {
  start: number;
  end: number;
}

export interface IEvaluationInfo {
  comment?: string;
  rating?: number;
  receivingTime?: number;
  requestId?: UUID;
}

export interface IAddressInfo {
  building?: string;
  city: string;
  country: string;
  house?: string;
  latitude: number;
  longitude: number;
  region: string;
  street?: string;
  structure?: string;
}

export interface IApprovedInfo {
  id: UUID;
  humanReadableId: string;
  userId: UUID;
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber: string;
  itinerantType: string;
  mvz: string;
  marriageCertificateNumber: string;
  delegatedById: UUID;
  supervisorId: UUID;
  positionId: UUID;
  positionName: string;
  organizationId: UUID;
  departmentId: UUID;
  departmentName: string;
  mobilePhone: string;
}

export interface IPersonInfo {
  departmentId: UUID;
  departmentName: string;
  firstName: string;
  humanReadableId: string;
  id: UUID;
  lastName: string;
  mobilePhone?: string;
  organizationId: UUID;
  patronymic: string;
  personnelNumber: string;
  positionId: UUID;
  positionName: string;
  userId: UUID;
  // TODO Провести актуализацию данных полей с беком. Описаны в свагере, по факту в ответе их нет.
  itinerantType: string;
  mvz: string;
  marriageCertificateNumber: string;
  delegatedById: UUID;
  supervisorId: UUID;
}

export interface ICarInfo {
  stateNumber: string;
  model: string;
  brand: string;
}

export interface ISort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface IPageable {
  unpaged: boolean;
  pageSize: number;
  pageNumber: number;
  sort: ISort;
  offset: number;
  paged: boolean;
}

export interface IOrderListResponse<T> {
  totalPages: number;
  totalElements: number;
  numberOfElements: number;
  pageable: IPageable;
  sort: ISort;
  number: number;
  size: number;
  content: T;
  first: boolean;
  last: boolean;
  empty: boolean;
}
