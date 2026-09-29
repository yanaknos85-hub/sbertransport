import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const ContractorDispatcherBase = t.intersection([
  t.type({
    firstName: t.string,
    lastName: t.string,
    id: tt.uuid,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);

export const ContractorDispatcher = t.intersection([
  ContractorDispatcherBase,
  t.type({
    phone: t.string,
    email: t.string,
    contractorId: tt.uuid,
    humanReadableId: t.string,
  }),
]);

