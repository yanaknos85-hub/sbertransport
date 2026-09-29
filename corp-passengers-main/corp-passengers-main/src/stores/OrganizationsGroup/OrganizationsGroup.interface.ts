import * as t from 'io-ts';
import { createPagination } from 'stores/Pagination/Pagination.interface';
import * as tt from '../../utils/io-ts';

export const OrganizationsGroup = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
  }),
  t.partial({
    internal: t.boolean,
    organizationIds: t.array(tt.uuid),
  }),
]);
export type OrganizationsGroup = t.TypeOf<typeof OrganizationsGroup>;

export const OrganizationsGroups = createPagination(OrganizationsGroup);
export type OrganizationsGroups = t.TypeOf<typeof OrganizationsGroups>;
