import {
  APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import {
  EMPLOYEE_PARAMS,
  GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS
} from 'constants/constants.api';
import * as t from 'io-ts';
import { MutationResultPair } from 'react-query';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Employee, EmployeeResponse } from 'stores/Employee/Employee.interface';
import { EmployeeStatus } from 'constants/constants.app';
import { Role } from 'stores/Roles/Roles.interface';
import { ignore } from 'utils';
import indexById from 'utils/indexById';
import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';

import { mkUseUploadEntity } from '../upload';

declare module 'api' {
  interface Cache {
    employeeResponse: {
      key: ['employeeResponse', UUID];
      value: {
        employeeResponse: EmployeeResponse;
        byId: Record<string, Employee>;
      };
    };
    employeeAllResponse: {
      key: ['employeeAllResponse', UUID];
      value: {
        employeeResponse: EmployeeResponse;
        byId: Record<string, Employee>;
      };
    };
    employee: { key: ['employee', UUID | null, UUID | null, UUID]; value: Employee | null };
  }
}

type CacheItem = TypeAtKey<['employeeResponse', UUID]>;
type CacheItemEmployee = TypeAtKey<['employee', UUID | null, UUID | null, UUID]>;

const raw2cache = (employeeResponse: EmployeeResponse): CacheItem => ({
  employeeResponse,
  byId: indexById(employeeResponse.content),
});

export const useEmployees = (orgId: UUID): APIQueryResult<CacheItem, Error> => useAPI(['employeeResponse', orgId], ({ http, process }) => http
  .get<EmployeeResponse>(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, { urlParams: { orgId } })
  .then(process.decodeResponseData(EmployeeResponse))
  .then(raw2cache)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  .catch(() => ({ employees: [] } as any))
);

export const useAllEmployees = (orgId: UUID): APIQueryResult<CacheItem, Error> => useAPI(['employeeAllResponse', orgId], async ({ http, process }) => {
  const total: number = await http
    .get<EmployeeResponse>(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, { urlParams: { orgId }, params: { size: 1 } })
    .then(res => res.data.totalElements);

  return http
    .get<EmployeeResponse>(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, {
      urlParams: { orgId },
      params: { size: total },
    })
    .then(process.decodeResponseData(EmployeeResponse))
    .then(raw2cache)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    .catch(() => ({ employees: [] } as any));
});

export const useEmployee = (
  orgId: UUID | null,
  depId: UUID | null,
  empId: UUID
): APIQueryResult<CacheItemEmployee, Error> => useAPI(['employee', orgId, depId, empId], ({ http, process }) => orgId && depId && empId
  ? http
    .get<Employee>(EMPLOYEE_PARAMS, {
      urlParams: {
        orgId, depId, empId,
      },
    })
    .then(process.decodeResponseData(Employee))
  : null
);

interface CreateUserInput {
  firstName: string;
  lastName: string;
  patronymic?: string;
  email: string;
}

const CreateUserResponse = t.type({
  firstName: t.string,
  lastName: t.string,
  id: tt.uuid,
});

type CreateUserResponse = t.TypeOf<typeof CreateUserResponse>;

// eslint-disable-next-line @stylistic/max-len
export const useCreateUser = (): MutationResultPair<CreateUserResponse, unknown, CreateUserInput, unknown> => useAPIMutation(
  ({ http, process }, user) => http.post<CreateUserResponse>('/users/', user).then(process.decodeResponseData(CreateUserResponse)),
  {
    onSuccess: ignore,
    onError: ignore,
  }
);

export const useCheckUser = (): (({
  organizationId,
  departmentId,
  userId,
}: {
  organizationId: UUID;
  userId: UUID;
  departmentId: UUID;
}) => Promise<void>) => {
  const { http } = useAppStoreContext();

  return ({
    organizationId, departmentId, userId,
  }: { organizationId: UUID; userId: UUID; departmentId: UUID }) => http
    .get(`/organizations/${organizationId}/departments/${departmentId}/employees/isUserExist/${userId}`, {
      // @ts-ignore
      hush: [404],
    })
    .then(ignore);
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const useDeleteUser = (): MutationResultPair<void, unknown, UUID, unknown> => useAPIMutation(({ http, process }, userId) => http.delete(`/users/${userId}`).then(ignore), {
  onSuccess: ignore,
  onError: ignore,
});

interface CreateEmployeeInput {
  organizationId: UUID;
  departmentId: UUID;
  mobilePhone?: string;
  email?: string;
  firstName: string;
  lastName: string;
  patronymic?: string;
  personnelNumber: string;
  positionId: UUID;
  roles: Role['code'][];
  status: 'ACTIVE' | 'INACTIVE';
  userId?: UUID;
}

const CreateEmployeeResponse = t.type({
  humanReadableId: t.string,
  id: tt.uuid,
  firstName: t.string,
  lastName: t.string,
  organizationId: tt.uuid,
  departmentId: tt.uuid,
  positionId: tt.uuid,
  status: t.keyof(EmployeeStatus),
});

type CreateEmployeeResponse = t.TypeOf<typeof CreateEmployeeResponse>;

export const useCreateEmployee = (): MutationResultPair<
  CreateEmployeeResponse,
  unknown,
  CreateEmployeeInput,
  unknown
> => useAPIMutation(
  ({ http, process }, {
    organizationId, departmentId, ...employee
  }) => http
    .post<Employee>(`/organizations/${organizationId}/departments/${departmentId}/employees/`, {
      departmentId,
      ...employee,
    })
  // FIXME: For some reason, the backend returns the newly-created Employee without the organizationId field.
  // we're filling it in here so that io-ts doesn't error
    .then(response => ({ ...response, data: { ...response.data, organizationId } }))
    .then(process.decodeResponseData(CreateEmployeeResponse)),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, result: employee, variables: { organizationId },
    }) => ignore(),
  }
);

export const useUpdateEmployee = (): MutationResultPair<
  void,
  unknown,
  { departmentId: UUID; updatedEmployee: Employee },
  unknown
> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  ({ http, process }, { departmentId, updatedEmployee: { organizationId, ...employee } }) => http
    .put<Employee>(
      `/organizations/${organizationId}/departments/${departmentId}/employees/${employee.id}`,
      employee
    )
    .then(ignore),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, result: _, variables: { updatedEmployee: employee }, process, t,
    }) => {
      cache.refetchQueries(['employeeRoles', employee.userId]);
      cache.invalidateQueries(['searchEmployee']);
      cache.refetchQueries(['employee', employee.organizationId, employee.departmentId, employee.id]);
    },
  }
);

// eslint-disable-next-line @stylistic/max-len
export const useDeleteEmployee = (): MutationResultPair<void, unknown, { employeeToDelete: Employee }, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  ({ http, process }, {
    employeeToDelete: {
      organizationId: orgId, departmentId: depId, id: empId,
    },
  }) => http
    .delete<Employee>(EMPLOYEE_PARAMS, {
      urlParams: {
        orgId, depId, empId,
      },
    })
    .then(ignore),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, variables: { employeeToDelete: currentEmployee }, process, t,
    }) => {
      process.processStatus(200, t.Employee.employeeDeleteSuccess);
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.Employee.error);
    },
  }
);

export const useUploadEmployees = mkUseUploadEntity('employee', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['employeeResponse']),
});

export const useUploadDepartments = mkUseUploadEntity('department', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['departments']),
});
