import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Vehicle = t.type({
  brandName: tt.nullable(t.string),
  model: tt.nullable(t.string),
  color: tt.nullable(t.string),
  registrationNumber: tt.nullable(t.string),
});

export type Vehicle = t.TypeOf<typeof Vehicle>;
