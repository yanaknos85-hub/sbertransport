import { MutationResultPair, QueryConfig } from 'react-query';
import * as t from 'io-ts';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import {
  ADD_DELEGATE,
  DELETE_DELEGATE,
  GET_DELEGATE,
  GET_DELEGATES,
  SEARCH_EMPLOYEE
} from '../constants/constants.api';
import { CargoAuto } from 'stores/CargoAuto/CargoAuto.interface';
import { UUID } from 'utils/io-ts';
import { EmployeeResponse } from 'stores/Employee/Employee.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { ignore } from '../utils';

declare module 'api' {
  interface Cache {
    delegateList: {
      key: ['delegateList', string, string, string];
      value: Delegate[];
    };
    delegate: {
      key: ['delegate', string, string, string];
      value: Delegate;
    };
  }
}

export const useGetDelegates = (
  {
    depId,
    orgId,
    supId,
  }: {
    depId: string;
    orgId: string;
    supId: string;
  },
  options: QueryConfig<Delegate[], unknown>
): APIQueryResult<Delegate[], unknown> => useAPI(
  ['delegateList', depId, orgId, supId],
  ({ http, process }) => http
    .get<Delegate[]>(GET_DELEGATES, {
      urlParams: {
        orgId, depId, supId,
      },
    })
    .then(process.decodeResponseData(t.array(Delegate))),
  options
);

export const usePostDelegates = (orgId: string, depId: string) => useAPIMutation(
  ({ http, process }, args: Omit<Delegate, 'id' | 'delegateEmployee'>) => http
    .post(
      `${ADD_DELEGATE}`,
      { ...args },
      {
        urlParams: { orgId, depId },
      }
    )
  // @ts-ignore
    .then<CargoAuto>(process.getResponseData),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.CargoAuto.AddSuccess);
      cache.refetchQueries(['delegateList']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error?.request?.status === 409) {
        logger.toMessage('error', t.Delegates.delegateDuplicateError);
      }
    },
  }
);

export const useGetEmployees = (
  orgId: UUID
// eslint-disable-next-line @stylistic/max-len
): MutationResultPair<EmployeeResponse, Error, { page: number; size: number; departmentName?: string }, unknown> => useAPIMutation(
  async ({ http, process }, {
    page, size = 20, departmentName,
  }) => http
    .get<EmployeeResponse>(SEARCH_EMPLOYEE, {
      urlParams: {
        orgId,
      },
      params: {
        fullName: departmentName,
        page,
        size,
      },
    })
    .then(process.decodeResponseData(EmployeeResponse)),
  {
    onSuccess: ignore,
  }
);

export const useDeleteDelegates = () => useAPIMutation(
  ({ http }, {
    orgId, depId, delId,
  }: { orgId: string; depId: string; delId: string }) => http.delete(DELETE_DELEGATE, {
    urlParams: {
      orgId, depId, delId,
    },
  }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Delegates.delegatesDeleteSuccess);
      cache.refetchQueries(['delegateList']);
    },
  }
);

export const useGetDelegate = ({
  depId,
  orgId,
  delId,
}: {
  depId: string;
  orgId: string;
  delId: string;
}): APIQueryResult<Delegate, unknown> => useAPI(['delegate', depId, orgId, delId], ({ http, process }) => http
  .get<Delegate>(GET_DELEGATE, {
    urlParams: {
      orgId, depId, delId,
    },
  })
  .then(process.decodeResponseData(Delegate))
);
