import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Driver = t.type({
  firstName: tt.nullable(t.string),
  lastName: tt.nullable(t.string),
  patronymic: tt.nullable(t.string),
  contactPhone: tt.nullable(t.string),
  rating: tt.nullable(t.number),
});

export type Driver = t.TypeOf<typeof Driver>;
