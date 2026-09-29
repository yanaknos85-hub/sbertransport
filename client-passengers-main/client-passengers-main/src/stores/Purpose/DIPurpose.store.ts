import { injectable } from 'inversify';
import { action, observable } from 'mobx';

import type { IPurposeStore, DatePurposeValidationObject } from './purpose.interface';

@injectable()
export class DIPurpose implements IPurposeStore {
  @observable
    purpose: DatePurposeValidationObject = { purposeId: '', isValid: false };

  @action.bound
  setPurpose(purposeChange: DatePurposeValidationObject): void {
    this.purpose = purposeChange;
  }
}
