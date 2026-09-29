import { LabeledValue } from 'antd/lib/select';
import React from 'react';

import { activeOptions, closedOptions } from 'shared/models/Approval.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

export const useApprovalFilterProps = (
  filter: string
): {
  transportType: TransportTypeEnum | undefined;
  setTransportType: React.Dispatch<React.SetStateAction<TransportTypeEnum | undefined>>;
  statusSetting: string;
  setStatusSetting: React.Dispatch<React.SetStateAction<string>>;
  options: LabeledValue[];
} => {
  const [transportType, setTransportType] = React.useState<TransportTypeEnum | undefined>();
  const [statusSetting, setStatusSetting] = React.useState('');

  const options = filter === 'active' ? activeOptions : closedOptions;
  return {
    transportType,
    setTransportType,
    statusSetting,
    setStatusSetting,
    options,
  };
};

export type ApprovalFilterProps = ReturnType<typeof useApprovalFilterProps>;
