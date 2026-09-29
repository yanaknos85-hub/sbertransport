import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

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
  t.type({ id: tt.uuid, name: t.string }),
  t.partial({
    autoparkName: tt.nullable(t.string),
    activeCarsCount: tt.nullable(t.number),
    contractor: tt.nullable(Contractor),
  }),
]);

export interface CacheAutoPark {
  autoparks: AutoPark[];
  byId: Record<string, AutoPark>;
}

export type AutoParksQuery = Record<string, unknown>;

export type AutoPark = t.TypeOf<typeof AutoPark>;
