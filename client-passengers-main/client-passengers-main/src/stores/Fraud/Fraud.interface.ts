export interface IFraudStoreValues {
  personalTripSplit: string | null;
  nonWorktimeTrip: string | null;
}

export interface IFraudStoreActions {
  prepareFraudComment: (valuesToInclude?: (keyof IFraudStoreValues)[]) => string;
}

export interface IFraudStore extends IFraudStoreValues, IFraudStoreActions {}
