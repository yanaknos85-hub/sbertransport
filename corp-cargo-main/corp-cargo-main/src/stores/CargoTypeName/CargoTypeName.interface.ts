import * as t from 'io-ts';

export const CargoTypeName = t.type({
  id: t.string,
  name: t.string,
  value: t.string,
});

export type CargoTypeName = t.TypeOf<typeof CargoTypeName>;

export const CargoTypeNames = t.array(CargoTypeName);
