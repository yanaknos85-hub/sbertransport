import * as t from 'io-ts';
import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';

export const ContractorDispatcherSelf = t.intersection([
  ContractorDispatcher,
  t.type({
    consent: t.boolean,
    organizationId: t.string,
    departmentId: t.string,
  }),
  t.partial({
    oauthId: t.string,
    originAutoparkId: t.string,
  }),
]);

export type ContractorDispatcherSelf = t.TypeOf<typeof ContractorDispatcherSelf>;
