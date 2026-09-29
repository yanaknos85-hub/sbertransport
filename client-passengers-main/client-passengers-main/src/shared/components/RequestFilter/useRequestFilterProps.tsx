import { TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';
import { useState } from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TRangePickerArg } from 'utils';

export const useRequestFilterProps = (): {
  dateRange: TRangePickerArg;
  setDateRange: React.Dispatch<React.SetStateAction<TRangePickerArg>>;
  transportType: TransportTypeEnum | undefined;
  setTransportType: React.Dispatch<React.SetStateAction<TransportTypeEnum | undefined>>;
  datePickerVisible: boolean;
  setDatePickerVisible: React.Dispatch<React.SetStateAction<boolean>>;
  status: TripStatusesEnum | undefined | string;
  setStatus: React.Dispatch<React.SetStateAction<TripStatusesEnum | undefined | string>>;
  statusFilter: string;
  setStatusFilter: React.Dispatch<React.SetStateAction<string>>;
  setPage?: React.Dispatch<React.SetStateAction<number>>;
} => {
  const [dateRange, setDateRange] = useState<TRangePickerArg>(null);
  const [transportType, setTransportType] = useState<TransportTypeEnum | undefined>();
  const [datePickerVisible, setDatePickerVisible] = useState(false);
  const [status, setStatus] = useState<TripStatusesEnum | undefined | string>();
  const [statusFilter, setStatusFilter] = useState('');

  return {
    dateRange,
    setDateRange,
    transportType,
    setTransportType,
    datePickerVisible,
    setDatePickerVisible,
    status,
    setStatus,
    statusFilter,
    setStatusFilter,
  };
};

export type RequestFilterProps = ReturnType<typeof useRequestFilterProps>;
