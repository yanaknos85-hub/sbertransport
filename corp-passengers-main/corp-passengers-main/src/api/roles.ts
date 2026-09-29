import {
  APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import { OrgStructureType } from 'constants/constants.app';
import {
  CREATE_ROLE,
  DELETE_ROLE,
  GET_ROLE_METHODS,
  GET_ROLE_SERVICES,
  GET_ROLES,
  GET_ROLES_BY_EMPLOYEE_AUTH,
  GET_ROLES_BY_EMPLOYEE_SUDIR,
  GET_SERVICE_METHODS,
  GET_SERVICES,
  METHOD,
  SERVICE,
  UI_INFO_SERVICE,
  UPDATE_ROLE
} from 'constants/constants.api';
import * as t from 'io-ts';
import {
  Method, Role, RoleCode, Service
} from 'stores/Roles/Roles.interface';

declare module 'api' {
  interface Cache {
    role: { key: ['roles']; value: Role[] };
    employeeRoles: { key: ['employeeRoles', string]; value: Role['code'][] };
    service: { key: ['services', string | null]; value: Service[] };
    methods: { key: ['methods', string, string | null]; value: Method[] };
  }
}

export const useEmployeeRoles = (userId: string, orgStructureType?: OrgStructureType): APIQueryResult<Role['code'][], Error> => useAPI(['employeeRoles', userId], ({ http, process }) => http
  .get<RoleCode[]>(
    orgStructureType === OrgStructureType.EXTERNAL ? GET_ROLES_BY_EMPLOYEE_AUTH : GET_ROLES_BY_EMPLOYEE_SUDIR,
    { urlParams: { userId } }
  )
  .then(process.decodeResponseData(t.array(RoleCode)))
  .then(roles => roles.map(({ code }) => code))
);

export const useRoles = (): APIQueryResult<Role[], Error> => useAPI(['roles'], ({ http, process }) => http.get<Role[]>(GET_ROLES).then(process.decodeResponseData(t.array(Role))));

export const useCreateRole = () => useAPIMutation(({ http }, role: Role) => http.post(CREATE_ROLE, role), {
  onSuccess: ({
    cache, variables: role, process, t,
  }) => {
    process.processStatus(200, t.Roles.AddSuccess);
    updateQueryCache(cache, ['roles'], roles => [...roles, role]);
  },
});

export const useUpdateRole = () => useAPIMutation(
  ({ http }, roleWithOriginalCode: Role & { originalCode: string }) => {
    const { originalCode, ...role } = roleWithOriginalCode;
    return http.put(UPDATE_ROLE, role, { urlParams: { code: originalCode } });
  },
  {
    onSuccess: ({
      cache, variables: roleWithOriginalCode, process, t,
    }) => {
      process.processStatus(200, t.Roles.EditSuccess);
      const { originalCode, ...role } = roleWithOriginalCode;
      return updateQueryCache(cache, ['roles'], roles => roles.map(r => (r.code === originalCode ? role : r)));
    },
  }
);

export const useDeleteRole = () => useAPIMutation(
  ({ http }, code: string) => http.delete<string>(DELETE_ROLE, { urlParams: { code } }),
  {
    onSuccess: ({
      cache, variables: code, process, t,
    }) => {
      process.processStatus(200, t.Roles.DeleteSuccess);
      updateQueryCache(cache, ['roles'], roles => roles.filter(r => r.code !== code));
    },
  }
);
// TODO:функция требует доработки будет реализовано в следующей итерации
export const useMethodRoles = (serviceId: string, roleCode: string | null): APIQueryResult<Method[], Error> => useAPI(['methods', serviceId, roleCode], ({ http, process }) => {
  const url = roleCode ? GET_ROLE_METHODS : GET_SERVICE_METHODS;
  const urlParams: Record<string, string> = roleCode ? { serviceId, roleCode } : { serviceId };
  return http
    .get<Method[]>(url, { urlParams })
    .then(process.decodeResponseData(t.array(Method)))
    .catch(() => []);
});

export const useSaveMethod = (text: string) => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, { role, indexes }: { role: string; indexes: string[] }): Promise<any> => http
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    .post<any>(`/${UI_INFO_SERVICE}/${METHOD}?serviceId=${text}`, { role, indexes })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ process, t }) => {
      process.processStatus(200, t.Roles.Methods.SaveSuccess);
    },
  }
);

export const useSaveService = () => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http }, { role, indexes }: { role: string; indexes: string[] }): Promise<any> => http.post<void>(`/${UI_INFO_SERVICE}/${SERVICE}/`, { role, indexes }),
  {
    onSuccess: ({ process, t }) => {
      process.processStatus(200, t.Roles.Services.SaveSuccess);
    },
  }
);

export const useServiceRoles = (roleCode: string | null): APIQueryResult<Service[], Error> => useAPI(['services', roleCode], ({ http, process }) => {
  const url = roleCode ? GET_ROLE_SERVICES : GET_SERVICES;
  const urlParams: Record<string, string> = roleCode ? { roleCode } : {};
  return http
    .get<Service[]>(url, { urlParams })
    .then(process.decodeResponseData(t.array(Service)))
    .catch(() => []);
});
