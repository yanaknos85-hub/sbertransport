import {
  Employee, EmployeeModel, RequestCancelerSetter, RequestParams
} from '@sber-sbertransport/mf-core';

import * as t from 'io-ts';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { createPagination, PaginationParams } from 'stores/Pagination/Pagination.interface';

export interface IDelegatesStore {
  delegates: DelegateModel[];
  delegateResponseInfo: Partial<DelegateResponseInfo>;
  candidatesToDelegates?: Record<string, EmployeeModel[]>;
  selfCandidatesToDelegates: EmployeeModel[];
  fullDelegateNames: Record<string, string>;
  addDelegate(delegate: Partial<DelegateModel>): Promise<void>;
  updateDelegate(delegate: Partial<DelegateModel>): Promise<void>;
  getDelegates(arg: PaginationParams): Promise<void>;
  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<void>;
  getCandidatesToDelegates(transTypeId: string, date: string): Promise<void>;
  deleteDelegate(delegateId: string): void;
  searchSelfDelegateCandidates(params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<EmployeeModel[]>;
}

export interface IDelegatesService {
  getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<Employee[]>;
  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<Employee[]>;
  getDelegates(args: Omit<getDeligatesArgs, 'transType' | 'delegateId'>): Promise<DelegateResponseInfo>;
  addDelegate(
    args: Omit<getDeligatesArgs, 'supId' | 'transType' | 'date' | 'delegateId'>,
    delegate: DelegateModel
  ): Promise<Delegate>;
  updateDelegate(
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
  page?: number;
  size?: number;
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

export const DelegateResponseInfo = createPagination(Delegate);

export type DelegateResponseInfo = t.TypeOf<typeof DelegateResponseInfo>;

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
