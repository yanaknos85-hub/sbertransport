import * as t from 'io-ts';

export const LocationAddress = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    city: t.string,
    region: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
  }),
]);

export type LocationAddress = t.TypeOf<typeof LocationAddress>;

export const Location = t.intersection([
  t.type({
    id: t.string,
    label: t.string,
    address: LocationAddress,
  }),
  t.partial({
    usages: t.number,
  }),
]);

export type Location = t.TypeOf<typeof Location>;
