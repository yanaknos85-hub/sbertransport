export interface DatePurposeValidationObject {
  purposeId: string;
  isValid: boolean;
}

export interface IPurposeStore {
  purpose: DatePurposeValidationObject;
}
