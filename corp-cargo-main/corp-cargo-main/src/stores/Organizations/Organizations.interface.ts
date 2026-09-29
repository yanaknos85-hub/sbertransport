import * as t from 'io-ts';
import { oneOf } from 'utils/io-ts';
import * as tt from '../../utils/io-ts';

export const ShortPosition = t.type({
  id: t.string,
  organizationId: t.string,
  positionName: t.string,
});

export const ShortDepartment = t.type({
  id: t.string,
  departmentName: t.string,
});

export const Organization = t.intersection([
  t.type({
    id: tt.uuid, officialName: t.string, address: t.string, status: oneOf('ACTIVE', 'INACTIVE'),
  }),
  t.partial({
    contacts: t.array(t.type({ type: t.string, value: t.string })),
    msrn: t.string,
    tid: t.string,
    digitId: t.number,
    organizationCode: t.number,
    organizationGroup: t.type({
      id: tt.uuid, name: t.string, internal: t.boolean,
    }),
    easupId: t.string,
  }),
]);
export type Organization = t.TypeOf<typeof Organization>;

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  sort: Sort,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});
const PageableUnion = t.union([Pageable, t.string]);

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});
export type PaginationParams = t.TypeOf<typeof PaginationParams>;

export const OrganizationResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Organization),
});
export type OrganizationResponse = t.TypeOf<typeof OrganizationResponse>;

export const FiltersOrganization = t.type({
  pageNumber: t.number,
  pageSize: t.number,
});

export type FiltersOrganization = t.TypeOf<typeof FiltersOrganization>;

export const OrganizationOption = t.type({
  officialName: t.string,
  id: t.string,
});
export const OrganizationOptions = t.array(OrganizationOption);

export const SearchOrganizationsResponse = t.type({
  organizations: OrganizationOptions,
});

export type SearchOrganizationsResponse = t.TypeOf<typeof SearchOrganizationsResponse>;
