import * as t from 'io-ts';

export const TransportServiceType = t.strict({
  name: t.string,
  rusName: t.string,
});

export type TransportServiceType = t.TypeOf<typeof TransportServiceType>;
