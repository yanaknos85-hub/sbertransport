import { Employee } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';

export const EmployeeResponseAllById = t.type({
  number: t.number,
  size: t.number,
  last: t.boolean,
  first: t.boolean,
  total: t.number,
  count: t.number,
  sort: t.type({
    field: t.string,
    direction: t.string,
  }),
  content: t.array(Employee),
});
export type EmployeeResponseAllById = t.TypeOf<typeof EmployeeResponseAllById>;
