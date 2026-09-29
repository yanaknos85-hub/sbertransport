import { Value } from 'shared/components/DateInput/types';

import { AllTransportTypes } from 'constants/constants.app';

import { FraudFilters } from '../../types';

export type SearchFormValues = Partial<{
  humanReadableId: string;
}>;

export type FilterFormValues = Partial<{
  transportType: string[];
  passengerName: string;
  purposes: string[];
  approverName: string;
  tripDate: Value;
  approveDate: Value;
}>;

export type FormValues = SearchFormValues & FilterFormValues;

export interface ActionSectionProps {
  filters: FraudFilters;
  filtersCount: number;
  setFilters: (filters: FraudFilters) => void;
  resetFilters: VoidFunction;
}

export type SelectTransportTypes =
  | typeof AllTransportTypes.YANDEX
  | typeof AllTransportTypes.PUBLIC
  | typeof AllTransportTypes.PERSONAL
  | typeof AllTransportTypes.TAXI
  | typeof AllTransportTypes.CARSHARING;
