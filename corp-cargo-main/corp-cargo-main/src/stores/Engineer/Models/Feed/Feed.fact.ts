import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Fact = t.type({
  tripFactWaitTime: tt.nullable(t.string),
  tripFactPrice: tt.nullable(tt.money),
  factSearchTime: tt.nullable(t.number),
  tripFactDistance: tt.nullable(t.string),
  tripFactDuration: tt.nullable(t.string),
  tripStartTime: tt.nullable(t.string),
});

export type Fact = t.TypeOf<typeof Fact>;
