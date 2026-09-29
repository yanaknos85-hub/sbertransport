import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { TypeAtKey } from '../index';
import { createPagination, PaginationParams } from 'utils/io-ts/pagination';

export const IntegrationParams = t.partial({
  contractorName: tt.nullable(t.string),
  contractorRusName: tt.nullable(t.string),
  integrationEmail: tt.nullable(t.string),
});

export const Contractor = t.intersection([
  t.type({
    name: tt.nullable(t.string),
    id: tt.nullable(tt.uuid),
    autoassign: t.boolean,
  }),
  t.partial({
    contactPersonInfo: tt.nullable(t.string),
    contactPersonPhone: tt.nullable(t.string),
    rating: tt.nullable(t.number),
    img: tt.nullable(t.string),
    regionIds: tt.nullable(t.array(tt.uuid)),
    integrationParams: tt.nullable(IntegrationParams),
    humanReadableId: tt.nullable(t.string),
    msrn: tt.nullable(t.string),
    tin: tt.nullable(t.string),
  }),
]);

export const AutoPark = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
    active: t.boolean,
  }),
  t.partial({
    autoparkName: tt.nullable(t.string),
    activeCarsCount: tt.nullable(t.number),
    contractor: tt.nullable(Contractor),
  }),
]);

export const AutoParks = createPagination(AutoPark);
export type AutoParks = t.TypeOf<typeof AutoParks>;

export type AutoParksQuery = Record<string, unknown> & PaginationParams;

export type CacheParks = TypeAtKey<['autopark-branches', tt.UUID, AutoParksQuery]>;
export type AutoPark = t.TypeOf<typeof AutoPark>;

export interface GetAutoparkParams {
  contractorId: string;
  autoparkId: string;
}

export interface CreateAutoparkParams {
  autopark: Omit<AutoPark, 'id'>;
}

export interface EditAutoparkParams {
  autoparkId: string;
  autopark: AutoPark;
}

export interface DeleteAutoparkParams {
  autoparkId: string;
}
