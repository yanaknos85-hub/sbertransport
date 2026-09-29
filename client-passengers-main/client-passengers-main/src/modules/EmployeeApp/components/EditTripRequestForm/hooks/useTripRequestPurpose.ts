import { useState, useEffect } from 'react';

import { DatePurposeValidationObject } from '../../CreateTripRequest/types/types';
import { TripPurpose } from 'stores/Trip/Trip.interface';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export interface TripRequestPurpose {
  purposeValidity: DatePurposeValidationObject;
  setPurposeValidity: React.Dispatch<React.SetStateAction<DatePurposeValidationObject>>;
}

export const useTripRequestPurpose = (initialValue: string): TripRequestPurpose => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();
  const getPurposeFromList = (purposeId: string | undefined): TripPurpose | undefined => (tripStore.purposes || []).find(({ id }) => purposeId === id);
  const initialPurpose = getPurposeFromList(initialValue) ? initialValue : '';
  const initialPurposeValidity: DatePurposeValidationObject = { purposeId: initialPurpose, isValid: !!initialValue };
  const [purposeValidity, setPurposeValidity] = useState<DatePurposeValidationObject>(initialPurposeValidity);

  useEffect(() => {
    setPurposeValidity(initialPurposeValidity);
  }, [initialPurpose]);

  return { purposeValidity, setPurposeValidity };
};
