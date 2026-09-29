import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const GeoZones = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
    code: t.union([t.string, t.number]),
  }),
  t.partial({
    parent_id: tt.uuid,
  }),
]);

export type GeoZones = t.TypeOf<typeof GeoZones>;
