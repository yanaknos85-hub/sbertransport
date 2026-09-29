import * as t from 'io-ts';

export const Role = t.intersection([
  t.type({
    code: t.string,
    name: t.string,
    description: t.string,
  }),
  t.partial({
    data_master: t.boolean,
    scopes: t.array(t.string),
  }),
]);
export type Role = t.TypeOf<typeof Role>;

export const RoleCode = t.strict({
  code: t.string,
});
export type RoleCode = t.TypeOf<typeof RoleCode>;

export const Service = t.strict({
  id: t.string,
  name: t.string,
  description: t.union([t.string, t.null]),
});

export type Service = t.TypeOf<typeof Service>;

export const Method = t.strict({
  id: t.string,
  name: t.string,
  description: t.union([t.string, t.null]),
});

export type Method = t.TypeOf<typeof Method>;
