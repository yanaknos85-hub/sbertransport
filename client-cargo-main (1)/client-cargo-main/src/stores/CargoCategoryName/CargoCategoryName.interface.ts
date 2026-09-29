import * as t from 'io-ts';

export const CargoCategoryName = t.type({
  id: t.string,
  name: t.string,
  value: t.string,
  unit: t.string,
});

export type CargoCategoryName = t.TypeOf<typeof CargoCategoryName>;

export const CargoCategoryNames = t.array(CargoCategoryName);
