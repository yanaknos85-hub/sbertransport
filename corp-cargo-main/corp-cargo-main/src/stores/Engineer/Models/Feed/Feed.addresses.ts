import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Address = t.type({
  building: tt.nullable(t.string),
  city: tt.nullable(t.string),
  country: tt.nullable(t.string),
  house: tt.nullable(t.string),
  region: tt.nullable(t.string),
  street: tt.nullable(t.string),
  structure: tt.nullable(t.string),
});

export type Address = t.TypeOf<typeof Address>;
