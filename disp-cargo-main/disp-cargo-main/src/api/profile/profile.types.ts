import * as t from 'io-ts';
import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';

export const ContractorDispatcherSelf = t.intersection([
  ContractorDispatcher,
  t.type({
    consent: t.boolean,
  }),
]);

export type ContractorDispatcherSelf = t.TypeOf<typeof ContractorDispatcherSelf>;
