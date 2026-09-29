import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, PaginationParams } from 'utils/io-ts/pagination';

export const AutoParkBranch = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
    active: t.boolean,
  }),
  t.partial({
    routingId: t.string,
  }),
]);

export type AutoParkBranch = t.TypeOf<typeof AutoParkBranch>;

export const AutoParkBranches = createPagination(AutoParkBranch);
export type AutoParkBranches = t.TypeOf<typeof AutoParkBranches>;

export interface AutoParkBranchesFilters extends PaginationParams {
  autoparkId?: string;
}
