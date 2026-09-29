import { APIQueryResult, useAPI } from 'api';
import * as t from 'io-ts';
import { DepartmentsResponse, PaginationParams } from 'stores/Department/Department.interface';
import { UUID } from 'utils/io-ts';
import { GET_ACTIVE_DEPARTMENTS } from 'constants/constants.api';

export const DepartmentSearchQuery = t.partial({
  humanReadableId: t.string,
  departmentName: t.string,
  code: t.string,
  status: t.string,
  location: t.string,
  page: t.number,
  size: t.number,
});
export type DepartmentSearchQuery = t.TypeOf<typeof DepartmentSearchQuery>;

type Projection = 'SELECT' | 'FULL';
export const DepartmentProjection = t.type({
  id: t.string,
  departmentName: t.string,
});
export type DepartmentProjection = t.TypeOf<typeof DepartmentProjection>;

declare module 'api' {
  interface Cache {
    searchDepartment: {
      key: ['searchDepartments', UUID, DepartmentSearchQuery | undefined, PaginationParams | undefined];
      value: DepartmentsResponse;
    };
    departmentProjection: { key: ['departmentProjection']; value: DepartmentProjection[] };
  }
}

export const useSearchDepartment = ({
  query,
  pagination,
  projection,
  orgId,
}: {
  query?: DepartmentSearchQuery;
  pagination?: PaginationParams;
  projection?: Projection | 'FULL';
  orgId: UUID;
}): APIQueryResult<DepartmentsResponse, Error> => useAPI(['searchDepartments', orgId, query, pagination], ({ http, process }) => http
  .get<DepartmentsResponse>(GET_ACTIVE_DEPARTMENTS, {
    urlParams: { orgId },
    params: {
      ...query, ...pagination, projection,
    },
  })
  .then(process.decodeResponseData(DepartmentsResponse))
);

export const useDepartmentProjection = (
  query?: DepartmentSearchQuery,
  pagination?: PaginationParams,
  projection?: Projection | 'ALL',
  orgId?: UUID
): APIQueryResult<DepartmentProjection[], Error> => useAPI(
  ['departmentProjection'],
  ({ http, process }) => http
    .get<DepartmentProjection[]>(GET_ACTIVE_DEPARTMENTS, {
      urlParams: { orgId: orgId || '' },
      params: {
        ...query, ...pagination, projection,
      },
    })
    .then(process.decodeResponseData(t.array(DepartmentProjection))),
  {
    refetchOnMount: true, cacheTime: 1, ...{ query, pagination },
  }
);
