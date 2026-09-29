import * as t from 'io-ts';

export const ServiceType = t.type({
  name: t.string,
  value: t.string,
});

export type ServiceType = t.TypeOf<typeof ServiceType>;
