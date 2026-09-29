import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Department = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    departmentName: tt.nullable(t.string),
  }),
]);

export type Department = t.TypeOf<typeof Department>;
