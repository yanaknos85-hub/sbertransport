import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Contractor = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    name: tt.nullable(t.string),
  }),
]);

export type Contractor = t.TypeOf<typeof Contractor>;
