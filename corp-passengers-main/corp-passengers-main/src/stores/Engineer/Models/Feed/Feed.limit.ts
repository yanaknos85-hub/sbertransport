import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Limit = t.type({
  id: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  limitType: tt.nullable(t.string),
  limitSharingType: tt.nullable(t.string),
});

export type Limit = t.TypeOf<typeof Limit>;
