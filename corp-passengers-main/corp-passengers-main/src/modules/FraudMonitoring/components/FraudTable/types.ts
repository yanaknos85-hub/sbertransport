import { Value } from 'shared/components/DateInput/types';

export type FraudFilters = Partial<{
  humanReadableId: string;
  transportType: string[];
  passengerName: string;
  purposes: string[];
  approverName: string;
  tripDate: Value;
  approveDate: Value;
}>;
