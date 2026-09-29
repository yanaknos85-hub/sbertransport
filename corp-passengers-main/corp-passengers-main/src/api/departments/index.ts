/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-unused-vars */
import {
  APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import { AxiosResponse, AxiosError } from 'axios';
import {
  DELETE_DEPARTMENT,
  DEPARTMENT_EDIT_PARAMS,
  DEPARTMENTS,
  DEPARTMENTS_ADD_PARAMS,
  GET_ACTIVE_DEPARTMENTS,
  GET_ALL_DEPARTMENTS,
  GET_DEPARTMENT,
  GET_DEPARTMENT_LEVEL,
  GET_DEPARTMENTS_BY_NAME_SUBSTRING,
  GET_ORGANIZATION_DEPARTMENT_LEVEL
} from 'constants/constants.api';
import * as t from 'io-ts';
import * as R from 'ramda';
import { MutationResultPair, QueryConfig } from 'react-query';
import { CreateDepartmentResponse, DepartmentFirstLevelChildInfo } from 'stores/Corporate/Corporate.interface';
import {
  Department,
  DepartmentsAllResponse,
  DepartmentsResponse,
  DepartmentSelectProjection,
  DepartmentLevelResponse
} from 'stores/Department/Department.interface';
import indexById from 'utils/indexById';
import { UUID } from 'utils/io-ts';

import { ignore, getErrorMessage } from '../../utils';
import { DepartmentLevels } from 'constants/constants.app';

declare module 'api' {
  interface Cache {
    departments: {
      key: ['departments', UUID];
      value: {
        departmentsResponse: DepartmentsResponse;
        byId: Record<string, Department>;
        byParent: Record<string, Department[]>;
      };
      parentId: Record<string, Department>;
    };
    allDepartments: {
      key: ['allDepartments', UUID];
      value: {
        departmentsAllResponse: DepartmentsAllResponse;
        byId: Record<string, DepartmentSelectProjection>;
        byParent: Record<string, DepartmentSelectProjection[]>;
      };
      parentId: Record<string, Department>;
    };
    allSelectDepartments: {
      key: ['allSelectDepartments', UUID, number];
      value: {
        departmentsAllResponse: DepartmentsAllResponse;
        byId: Record<string, DepartmentSelectProjection>;
        byParent: Record<string, DepartmentSelectProjection[]>;
      };
      parentId: Record<string, Department>;
    };
    allFullDepartments: {
      key: ['allFullDepartments', UUID];
      value: {
        departmentsResponse: DepartmentsResponse;
        byId: Record<string, Department>;
        byParent: Record<string, Department[]>;
      };
      parentId: Record<string, Department>;
    };
    departmentsByNameSubstring: {
      key: ['departmentsByNameSubstring', UUID, string];
      value: {
        departmentsByNameSubstringResponse: DepartmentsResponse;
      };
    };
    allChildrenDepartments: {
      key: ['allChildrenDepartments', UUID];
      value: {
        departmentsResponse: DepartmentsResponse;
      };
    };
    departmentChild: {
      key: ['departmentChild', UUID, UUID];
      value: {
        departmentChild: DepartmentFirstLevelChildInfo[];
      };
    };
    department: {
      key: ['department', UUID | null, UUID | null | undefined];
      value: Department | null;
    };
    test: {
      key: ['departmentLevel', UUID];
      value: DepartmentLevelResponse[];
    };
  }
}

type CacheItem = TypeAtKey<['departments', UUID]>;
type CacheAllItems = TypeAtKey<['allDepartments', UUID]>;
type CacheAllFullItems = TypeAtKey<['allFullDepartments', UUID]>;
type CacheItemByNameSubstring = TypeAtKey<['departmentsByNameSubstring', UUID, string]>;
type CacheItemAllChilds = TypeAtKey<['allChildrenDepartments', UUID]>;
type CacheItemWithChild = TypeAtKey<['departmentChild', UUID, UUID]>;
type CacheItemDepartment = TypeAtKey<['department', UUID | null, UUID | null | undefined]>;

const raw2cache = (departmentsResponse: DepartmentsResponse): CacheItem => ({
  departmentsResponse,
  byId: indexById(departmentsResponse?.content),
  byParent: R.groupBy<Department>(R.propOr(undefined, 'parentId'), departmentsResponse?.content),
});

const raw2cacheAllDepartments = (departmentsAllResponse: DepartmentsAllResponse): CacheAllItems => ({
  departmentsAllResponse,
  byId: indexById(departmentsAllResponse),
  byParent: R.groupBy<DepartmentSelectProjection>(R.propOr(undefined, 'parentId'), departmentsAllResponse),
});

const raw2cacheByNameSubstring = (
  departmentsByNameSubstringResponse: DepartmentsResponse
): CacheItemByNameSubstring => ({
  departmentsByNameSubstringResponse,
});

const raw2cacheWithChilds = (departmentChild: DepartmentFirstLevelChildInfo[]): CacheItemWithChild => ({
  departmentChild,
});

const raw2cacheWithAllChilds = (departmentsResponse: DepartmentsResponse): CacheItemAllChilds => ({
  departmentsResponse,
});

export const useDepartments = (orgId: UUID): APIQueryResult<CacheItem, Error> => useAPI(['departments', orgId], ({ http, process }) => http
  .get<DepartmentsResponse>(GET_ACTIVE_DEPARTMENTS, { urlParams: { orgId } })
  .then(process.decodeResponseData(DepartmentsResponse))
  .then(raw2cache)
  .catch(() => ({ departments: [] } as any))
);

export const useDepartmentsByNameSubstring = (
  orgId: UUID,
  code: string,
  options?: QueryConfig<CacheItemByNameSubstring, Error>
): APIQueryResult<CacheItemByNameSubstring, Error> => useAPI(
  ['departmentsByNameSubstring', orgId, code],
  ({ http, process }) => http
    .get<DepartmentsResponse>(GET_DEPARTMENTS_BY_NAME_SUBSTRING, {
      urlParams: { orgId },
      params: { code },
    })
    .then(process.decodeResponseData(DepartmentsResponse))
    .then(raw2cacheByNameSubstring)
    .catch(() => [] as any),
  { refetchOnMount: false, ...options }
);

export const useAllChildrenDepartments = (orgId: UUID, depId: UUID): APIQueryResult<CacheItemAllChilds, Error> => useAPI(['allChildrenDepartments', orgId], ({ http, process }) => http
  .get<DepartmentsResponse>(`/organizations/${orgId}/departments/${depId}/children?size=999999999`)
  .then(process.decodeResponseData(DepartmentsResponse))
  .then(raw2cacheWithAllChilds)
  .catch(() => ({ allChildrenDepartments: [] } as any))
);

export const useAllDepartments = (orgId: UUID): APIQueryResult<CacheAllItems, Error> => useAPI(['allDepartments', orgId], async ({ http, process }) => {
  let total: number | null = null;

  total = await http
    .get<DepartmentsResponse>(GET_ALL_DEPARTMENTS, {
      urlParams: { orgId },
      params: { size: 1 },
    })
    .then(res => res.data.totalElements);

  return http
    .get<DepartmentsAllResponse>(GET_ALL_DEPARTMENTS, {
      urlParams: { orgId },
      params: {
        projection: 'SELECT',
        ...(Boolean(total) && { size: total }),
      },
    })
    .then(process.decodeResponseData(DepartmentsAllResponse))
    .then(raw2cacheAllDepartments)
    .catch(() => ({ departments: [] } as any));
});

export const useAllFullDepartments = (orgId: UUID): APIQueryResult<CacheAllFullItems, Error> => useAPI(['allFullDepartments', orgId], async ({ http, process }) => {
  let total: number | null = null;

  total = await http
    .get<DepartmentsResponse>(GET_ALL_DEPARTMENTS, {
      urlParams: { orgId },
      params: { size: 1 },
    })
    .then(res => res.data.totalElements);

  return http
    .get<DepartmentsResponse>(GET_ALL_DEPARTMENTS, {
      urlParams: { orgId },
      params: {
        ...(Boolean(total) && { size: total }),
      },
    })
    .then(process.decodeResponseData(DepartmentsResponse))
    .then(raw2cache)
    .catch(() => ({ departments: [] } as any));
});

export const useSelectDepartmentsSearch = (
  orgId: UUID
): MutationResultPair<DepartmentsResponse, Error, { page: number; departmentName?: string }, unknown> => useAPIMutation(
  async ({ http, process }, { page, departmentName }) => http
    .get<DepartmentsResponse>(GET_ALL_DEPARTMENTS, {
      urlParams: { orgId },
      params: {
        projection: 'FULL',
        status: 'ACTIVE',
        size: 20,
        page,
        departmentName,
      },
    })
    .then(process.decodeResponseData(DepartmentsResponse)),
  {
    onSuccess: ({ cache, result }) => {
      cache.setQueryData(['allFullDepartments', orgId], raw2cache(result));
    },
  }
);

export const useCreateDepartment = (): MutationResultPair<
  CreateDepartmentResponse,
  unknown,
  Omit<Department, 'id'>,
  unknown
> => useAPIMutation(
  ({ http, process }, department: Omit<Department, 'id'>) => http
    .post<CreateDepartmentResponse>(DEPARTMENTS_ADD_PARAMS, department, {
      urlParams: { orgId: department.organizationId },
    })
    .then(process.decodeResponseData(CreateDepartmentResponse)),
  {
    onSuccess: ({
      cache, result: department, process,
    }) => {
      process.processStatus(200, 'Департамент успешно создан');
    },
    onError: ({
      error, logger, t,
    }) => {
      logger.toMessage(
        'error',
        (error as AxiosError).response?.status === 409 ? t.global.duplicateError : getErrorMessage(error as AxiosError)
      );
    },
  }
);

export const useUpdateDepartment = (): MutationResultPair<
  AxiosResponse<unknown>,
  unknown,
  {
    orgId: UUID;
    department: Department;
  },
  unknown
> => useAPIMutation(
  ({ http }, { orgId, department }: { orgId: UUID; department: Department }) => (
    http.put(DEPARTMENT_EDIT_PARAMS, department, { urlParams: { orgId, depId: department.id } })
  ),
  {
    onSuccess: ({
      cache, variables: { orgId, department }, process,
    }) => {
      process.processStatus(200, 'Информация о департаменте успешно обновлена');
      cache.refetchQueries(['departments']);
    },
    onError: ({
      error, logger, t,
    }) => {
      logger.toMessage(
        'error',
        (error as AxiosError).response?.status === 409 ? t.global.duplicateError : getErrorMessage(error as AxiosError)
      );
    },
  }
);

export const useDeleteDepartment = (
  organizationId: UUID
): MutationResultPair<void, unknown, { orgId: UUID; depId: UUID }, unknown> => useAPIMutation(
  ({ http }, { orgId, depId }: { orgId: UUID; depId: UUID }) => http
    .delete<number>(DELETE_DEPARTMENT, { urlParams: { orgId, depId } })
    .then(ignore),
  {
    onSuccess: ({ cache, variables: { orgId, depId } }) => ignore(),
  }
);

export const useGetDepartmentFirstLevelChild = (
  orgId: UUID,
  depId: UUID | string,
  options: QueryConfig<CacheItemWithChild, Error> = {}
): APIQueryResult<CacheItemWithChild, Error> => useAPI(
  ['departmentChild', orgId, depId as UUID],
  ({ http, process }) => http
    .get<DepartmentFirstLevelChildInfo[]>(`/organizations/:orgId/${DEPARTMENTS}/${depId}/children`, {
      urlParams: { orgId, depId },
    })
    .then(process.decodeResponseData(t.array(DepartmentFirstLevelChildInfo)))
    .then(raw2cacheWithChilds),
  {
    cacheTime: 0, refetchOnMount: true, ...options,
  }
);

export const useDepartment = (
  orgId: UUID | null,
  depId: UUID | null | undefined
): APIQueryResult<CacheItemDepartment> => useAPI(['department', orgId, depId], ({ http, process }) => orgId && depId
  ? http
    .get<Department>(GET_DEPARTMENT, { urlParams: { orgId, depId } })
    .then(process.decodeResponseData(Department))
  : null
);

export const useSelectDepartmentsForDepartmentLevel = (
  orgId: UUID,
  departments: DepartmentLevels
): MutationResultPair<string[], Error, unknown, unknown> => useAPIMutation(
  async ({ http, process }) => http
    .post<string[]>(GET_DEPARTMENT_LEVEL, departments, {
      urlParams: { orgId },
    })
    .then(process.getResponseData),
  {
    onSuccess: ignore,
  }
);

export const useGetDepartmentLevel = (orgId: UUID): APIQueryResult<DepartmentLevelResponse[]> => (
  useAPI(['departmentLevel', orgId], ({ http, process }) => (
    http
      .get<DepartmentLevelResponse[]>(GET_ORGANIZATION_DEPARTMENT_LEVEL, { urlParams: { orgId } })
      .then(process.getResponseData)
  ))
);
