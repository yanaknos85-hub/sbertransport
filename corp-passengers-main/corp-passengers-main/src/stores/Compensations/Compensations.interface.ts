import * as t from 'io-ts';

export const PublicCompensation = t.type({
  name: t.string,
  rusName: t.string,
});

export type PublicCompensation = t.TypeOf<typeof PublicCompensation>;
