import { Select } from 'antd';
import React from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeOptions } from 'stores/Trip/Trip.interface';
import { LabeledValue } from 'utils';

import { ApprovalFilterProps } from './hooks/useApprovalFilterProps';

export const ApprovalFilter: React.FC<ApprovalFilterProps> = props => {
  const {
    setTransportType, setStatusSetting, options, statusSetting,
  } = props;

  const _setTransportType = (option: LabeledValue<TransportTypeEnum>): void => {
    setTransportType(option.value);
  };
  return (
    <>
      <Select
        labelInValue={true}
        onChange={_setTransportType}
        options={TransportTypeOptions}
        style={{ width: '100%' }}
        allowClear={true}
      />
      <Select
        value={statusSetting}
        onChange={value => {
          setStatusSetting(value);
        }}
        allowClear={true}
        style={{ width: '100%', marginTop: '16px' }}
      >
        {options.map(option => (
          <Select.Option key={option.value} value={option.value}>
            {option.label}
          </Select.Option>
        ))}
      </Select>
    </>
  );
};
