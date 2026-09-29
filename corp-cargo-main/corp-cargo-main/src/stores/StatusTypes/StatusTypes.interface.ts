import * as t from 'io-ts';

export const AvailableStatus = t.intersection([
  t.type({
    name: t.string,
    rusName: t.string,
  }),
  t.partial({
    finalStatus: t.boolean,
    color: t.string,
    index: t.number,
  }),
]);

export type AvailableStatus = t.TypeOf<typeof AvailableStatus>;
