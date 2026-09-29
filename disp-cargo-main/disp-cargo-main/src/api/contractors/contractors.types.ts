import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, PaginationParams } from 'utils/io-ts/pagination';

export const IntegrationsParams = t.partial({
  contractorName: t.string,
  contractorRusName: t.string,
  integrationEmail: t.string,
});

export type IntegrationsParams = t.TypeOf<typeof IntegrationsParams>;

export const Contractor = t.intersection([
  t.type({
    id: t.string,
    name: t.string,
    msrn: t.string,
    tin: t.string,
    humanReadableId: t.string,
    autoassign: t.boolean,
  }),
  t.partial({
    contactPersonInfo: t.string,
    contactPersonPhone: t.string,
    integrationParams: IntegrationsParams,
  }),
]);

export type Contractor = t.TypeOf<typeof Contractor>;

export const Autopark = t.intersection([
  t.type({
    name: t.string,
    id: tt.uuid,
    humanReadableId: t.string,
    autoassign: t.boolean,
  }),
  t.partial({
    tin: t.string,
    digitId: t.number,
    mainDispatcher: t.intersection([
      t.type({
        id: tt.uuid,
        lastName: t.string,
        firstName: t.string,
        contractorId: tt.uuid,
        autoassign: t.boolean,
        consent: t.boolean,
      }),
      t.partial({
        patronymic: t.string,
        humanReadableId: t.string,
        phone: t.string,
        email: t.string,
      }),
    ]),
  }),
]);
export type Autopark = t.TypeOf<typeof Autopark>;

export const Autoparks = createPagination(Autopark);
export type Autoparks = t.TypeOf<typeof Autoparks>;

export interface AutoparksFilters extends PaginationParams { }
