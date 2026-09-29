import {
  Employee, EmployeeModel, RequestCancelerSetter, RequestParams
} from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

export interface IDelegatesStore {
  delegates: DelegateModel[];
  candidatesToDelegates?: Record<string, EmployeeModel[]>;
  selfCandidatesToDelegates: EmployeeModel[];
  namesWithInitials: Record<string, string>;
  addDelegate(delegate: Partial<DelegateModel>): void;
  getDelegates(): Promise<void>;
  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<void>;
  getCandidatesToDelegates(transTypeId: string, date: string): Promise<void>;
  deleteDelegate(delegateId: string): void;
  initStore(): void;
  searchSelfDelegateCandidates(params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<EmployeeModel[]>;
}

export interface IDelegatesService {
  getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<Employee[]>;
  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<Employee[]>;
  getDelegates(args: Omit<getDeligatesArgs, 'transType' | 'delegateId'>): Promise<Delegate[]>;
  addDelegate(
    args: Omit<getDeligatesArgs, 'supId' | 'transType' | 'date' | 'delegateId'>,
    delegate: DelegateModel
  ): Promise<Delegate>;
  deleteDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transType' | 'date'>): Promise<number>;
  searchSelfDelegateCandidates(
    transportType: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]>;
}

export interface getDeligatesArgs {
  orgId: string;
  depId: string;
  supId: string;
  transType: string;
  delegateId: string;
  date?: string;
}

export const Delegate = t.strict({
  id: t.string,
  supervisorId: t.string,
  delegateId: t.string,
  startDate: t.string,
  endDate: t.string,
  transportType: t.string,
  delegateEmployee: Employee,
});

export type Delegate = t.TypeOf<typeof Delegate>;

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

export const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});

export const IODelegateList = t.type({
  content: t.array(Delegate),
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: Pageable,
  size: t.number,
  sort: Sorted,
  totalElements: t.number,
  totalPages: t.number,
});

export type TDelegateList = t.TypeOf<typeof IODelegateList>;

export class DelegateModel implements Delegate {
  id: string;

  supervisorId: string;

  delegateId: string;

  startDate: string;

  endDate: string;

  transportType: string;

  delegateEmployee: Employee;

  constructor(delegate: Delegate) {
    this.id = delegate.id;
    this.supervisorId = delegate.supervisorId;
    this.delegateId = delegate.delegateId;
    this.startDate = delegate.startDate;
    this.endDate = delegate.endDate;
    this.transportType = delegate.transportType;
    this.delegateEmployee = delegate.delegateEmployee;
  }
}
