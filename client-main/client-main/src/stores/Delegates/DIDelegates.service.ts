import * as mfCore from '@sber-sbertransport/mf-core';

import axios from 'axios';
import { inject, injectable } from 'inversify';

import {
  ADD_DELEGATE,
  DELETE_DELEGATE,
  GET_CANDIDATES_IN_DELEGATES_PARAMS,
  GET_DELEGATES,
  GET_SELF_CONDIDATES_TO_DELEGATES,
  UPDATE_DELEGATE
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import {
  Delegate, DelegateModel, DelegateResponseInfo, IDelegatesService, getDeligatesArgs
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
      .get<{ content: mfCore.Employee[] }>(GET_SELF_CONDIDATES_TO_DELEGATES, {
        params: { date },
        urlParams: {
          transportType,
        },
      })
      .then(data => this.process.getResponseData(data).content);
  }

  getDelegates(args: Omit<getDeligatesArgs, 'transportTypeId' | 'delegateId'>): Promise<DelegateResponseInfo> {
    const {
      depId, orgId, supId, ...params
    } = args;

    return this.http
      .get<DelegateResponseInfo>(GET_DELEGATES, {
        params,
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

  updateDelegate(
    args: Omit<getDeligatesArgs, 'supId' | 'transportType' | 'date' | 'delegateId'>,
    delegate: DelegateModel
  ): Promise<Delegate> {
    const { depId, orgId } = args;

    return this.http
      .put<Delegate>(UPDATE_DELEGATE, { ...delegate }, {
        urlParams: {
          orgId, depId, delId: delegate.id,
        },
      })
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

  searchSelfDelegateCandidates(
    transportType: string,
    params: mfCore.RequestParams,
    cancelerSetter?: mfCore.RequestCancelerSetter
  ): Promise<mfCore.Employee[]> {
    return this.http
      .get<{ content: mfCore.Employee[] }>(GET_SELF_CONDIDATES_TO_DELEGATES, {
        params,
        urlParams: { transportType },
        cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
      })
      .then(data => this.process.getResponseData(data.data.content));
  }
}
