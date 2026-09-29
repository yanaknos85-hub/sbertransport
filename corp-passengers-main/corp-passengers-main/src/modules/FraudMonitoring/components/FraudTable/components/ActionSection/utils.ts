import { TransportType } from 'stores/TransportTypes/TransportTypes.interface';

import { LabeledValue } from 'utils';

import { FilterFormValues } from './types';
import { FraudFilters } from '../../types';

export const convertTransportTypesToOptions = (
  allTransportTypes: TransportType[],
  availableTransportTypes: string[]
) => {
  return allTransportTypes
    .filter(({ name }) => availableTransportTypes.includes(name))
    .map<LabeledValue>(({ name, rusName }) => ({ value: name, label: rusName }));
};

export const generateInitialFilterValues = (filters: FraudFilters): FilterFormValues => {
  const {
    approveDate, approverName, passengerName, purposes, transportType, tripDate,
  } = filters;

  return {
    purposes,
    transportType,
    tripDate,
    approveDate,
    approverName,
    passengerName,
  };
};
