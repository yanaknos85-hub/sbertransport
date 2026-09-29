import * as t from 'io-ts';

export const Status = t.type({
  color: t.string,
  finalStatus: t.boolean,
  name: t.string,
  rusName: t.string,
});

export type Status = t.TypeOf<typeof Status>;
