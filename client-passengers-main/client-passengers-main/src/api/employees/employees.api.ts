import {
  APIQueryResult, TypeAtKey, useAPI
} from 'api';
import {
  GET_ALL_EMPLOYEES,
  GET_USER_AVATAR_BY_ID
} from './employees.constants';

import { UUID } from 'utils/io-ts';
import indexById from 'utils/indexById';
import { Employee } from '@sber-sbertransport/mf-core';
import { EmployeeResponseAllById } from './employees.types';

declare module 'api' {
  interface Cache {
    employeesAllByIdResponse: {
      key: ['employeesAllByIdResponse', UUID[]];
      value: {
        employeeResponse: EmployeeResponseAllById;
        byId: Record<string, Employee>;
      };
    };
    employeeAvatar: {
      key: ['employeeAvatar', UUID];
      value: string | null;
    };
  }
}

type CacheItemEmployeesById = TypeAtKey<['employeesAllByIdResponse', UUID[]]>;

const raw2cacheAllEmployees = (employeeResponse: EmployeeResponseAllById): CacheItemEmployeesById => ({
  employeeResponse,
  byId: indexById(employeeResponse.content),
});

export const useAllEmployeesById = (ids: UUID[]): APIQueryResult<CacheItemEmployeesById, Error> => {
  const employees = ids.join(',');
  return useAPI(['employeesAllByIdResponse', ids], ({ http, process }) => http
    .get<EmployeeResponseAllById>(GET_ALL_EMPLOYEES, { params: { employees } })
    .then(process.decodeResponseData(EmployeeResponseAllById))
    .then(raw2cacheAllEmployees)
    .catch(() => ({ employees: [] } as any))
  );
};

export const useEmployeeAvatar = (id: UUID): APIQueryResult<string | null, Error> => {
  return useAPI(['employeeAvatar', id], ({ http }) => http
    .get<Blob>(GET_USER_AVATAR_BY_ID, { urlParams: { id }, responseType: 'blob' })
    .then(({ data: blob }) => {
      if (blob && blob.size > 0) {
        return URL.createObjectURL(blob);
      }

      return null;
    })
    .catch(() => null)
  );
};
