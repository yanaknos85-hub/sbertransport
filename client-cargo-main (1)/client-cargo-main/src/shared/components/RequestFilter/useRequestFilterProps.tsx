import { useState } from 'react';
import { TRangePickerArg } from 'utils';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

export const useRequestFilterProps = (): {
  dateRange: TRangePickerArg;
  setDateRange: React.Dispatch<React.SetStateAction<TRangePickerArg>>;
  transportType: TransportTypeEnum | undefined;
  setTransportType: React.Dispatch<React.SetStateAction<TransportTypeEnum | undefined>>;
  datePickerVisible: boolean;
  setDatePickerVisible: React.Dispatch<React.SetStateAction<boolean>>;
} => {
  const [dateRange, setDateRange] = useState<TRangePickerArg>(null);
  const [transportType, setTransportType] = useState<TransportTypeEnum | undefined>();
  const [datePickerVisible, setDatePickerVisible] = useState(false);

  return {
    dateRange,
    setDateRange,
    transportType,
    setTransportType,
    datePickerVisible,
    setDatePickerVisible,
  };
};

export type RequestFilterProps = ReturnType<typeof useRequestFilterProps>;
