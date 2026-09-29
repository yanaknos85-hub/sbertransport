import { useState } from 'react';

import { DatePurposeValidationObject } from '../../CreateTripRequest/types/types';

export interface TripRequestPurpose {
  purposeValidity: DatePurposeValidationObject;
  setPurposeValidity: React.Dispatch<React.SetStateAction<DatePurposeValidationObject>>;
}

export const useTripRequestPurpose = (initialValue: string): TripRequestPurpose => {
  const initialPurposeValidity: DatePurposeValidationObject = { purposeId: initialValue, isValid: !!initialValue };

  const [purposeValidity, setPurposeValidity] = useState<DatePurposeValidationObject>(initialPurposeValidity);

  return { purposeValidity, setPurposeValidity };
};
