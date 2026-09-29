import { injectable } from 'inversify';
import { observable } from 'mobx';

import { FRAUD_COMMENT_SEPARATOR } from './Fraud.constants';
import { IFraudStore, IFraudStoreValues } from './Fraud.interface';

@injectable()
export class DIFraudStore implements IFraudStore {
  @observable
    personalTripSplit: string | null = null;

  @observable
    nonWorktimeTrip: string | null = null;

  prepareFraudComment(valuesToInclude?: (keyof IFraudStoreValues)[]) {
    let fraudEntries = Object.entries(this).filter(([_, value]) => typeof value === 'string');

    if (valuesToInclude) {
      fraudEntries = fraudEntries.filter(([key]) => valuesToInclude.includes(key as keyof IFraudStoreValues));
    }

    const fraudValues = fraudEntries.map(([_, value]) => value);
    const fraudComment = fraudValues.join(FRAUD_COMMENT_SEPARATOR);

    return fraudComment;
  }
}
