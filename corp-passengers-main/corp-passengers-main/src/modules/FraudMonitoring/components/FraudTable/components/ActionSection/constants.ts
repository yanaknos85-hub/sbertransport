import { AllTransportTypes } from 'constants/constants.app';

import { FormValues } from './types';

export const formValues: Record<keyof FormValues, keyof FormValues> = {
  humanReadableId: 'humanReadableId',
  transportType: 'transportType',
  passengerName: 'passengerName',
  approverName: 'approverName',
  purposes: 'purposes',
  tripDate: 'tripDate',
  approveDate: 'approveDate',
};

export const selectTransportTypes: string[] = [
  AllTransportTypes.YANDEX,
  AllTransportTypes.PUBLIC,
  AllTransportTypes.PERSONAL,
  AllTransportTypes.TAXI,
  AllTransportTypes.CARSHARING,
];
