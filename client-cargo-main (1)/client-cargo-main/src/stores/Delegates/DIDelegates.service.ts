import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  ADD_DELEGATE,
  DELETE_DELEGATE,
  GET_CANDIDATES_IN_DELEGATES_PARAMS,
  GET_DELEGATES,
  GET_SELF_CONDIDATES_TO_DELEGATES
} from 'constants/constants.env';

import {
  Delegate, DelegateModel, getDeligatesArgs,
  IDelegatesService
} from './Delegates.interface';

@injectable()
export class DIDelegatesService implements IDelegatesService {
  @inject(TYPES.IHttpService)
  private http!: mfCore.IHttpService;

  @inject(TYPES.IResponseService)
  private process!: mfCore.IResponseService;

  getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<mfCore.Employee[]> {
    const {
      depId, orgId, supId, transType, date,
    } = args;

    return this.http
      .get<mfCore.Employee[]>(GET_CANDIDATES_IN_DELEGATES_PARAMS, {
        params: { date },
        urlParams: {
          orgId,
          depId,
          supId,
          transType,
        },
      })
      .then(this.process.getResponseData);
  }

  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<mfCore.Employee[]> {
    return this.http
      .get<mfCore.Employee[]>(GET_SELF_CONDIDATES_TO_DELEGATES, {
        params: { date },
        urlParams: {
          transportType,
        },
      })
      .then(this.process.getResponseData);
  }

  getDelegates(args: Omit<getDeligatesArgs, 'transportTypeId' | 'delegateId'>): Promise<Delegate[]> {
    const {
      depId, orgId, supId, date,
    } = args;

    return this.http
      .get<Delegate[]>(GET_DELEGATES, {
        params: { date },
        urlParams: {
          orgId,
          depId,
          supId,
        },
      })
      .then(this.process.getResponseData);
  }

  addDelegate(
    args: Omit<getDeligatesArgs, 'supId' | 'transportType' | 'date' | 'delegateId'>,
    delegate: DelegateModel
  ): Promise<Delegate> {
    const { depId, orgId } = args;

    return this.http
      .post<Delegate>(ADD_DELEGATE, { ...delegate }, { urlParams: { orgId, depId } })
      .then(this.process.getResponseData);
  }

  deleteDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transTypeId' | 'date'>): Promise<number> {
    const {
      depId, orgId, delegateId,
    } = args;

    return this.http
      .delete<number>(DELETE_DELEGATE, {
        urlParams: {
          orgId, depId, delId: delegateId,
        },
      })
      .then(this.process.getResponseStatus);
  }
}
