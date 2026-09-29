import { APIQueryResult, useAPI } from 'api';

import { GET_DEPARTMENT } from 'constants/constants.env';

import { TDepartment as Department } from 'stores/Corporate/Corporate.interface';

declare module 'api' {
  interface Cache {
    department: {
      key: ['department', string | undefined, string | undefined];
      value: Department;
    };
  }
}

export const useDepartment = (
  organizationId: string | undefined,
  departmentId: string | undefined
): APIQueryResult<Department> => useAPI(['department', organizationId, departmentId], ({ http, process }) => http
  .get<Department>(GET_DEPARTMENT, { urlParams: { orgId: organizationId!, depId: departmentId! } })
  .then(process.getResponseData),
{
  enabled: organizationId && departmentId,
}
);
