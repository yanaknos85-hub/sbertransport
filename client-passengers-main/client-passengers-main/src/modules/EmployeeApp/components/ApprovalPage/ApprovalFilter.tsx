import { Select } from 'antd';
import React from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';

import { LabeledValue } from 'utils';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeOptions } from 'stores/Trip/Trip.interface';
import { activeOptions, closedOptions } from 'shared/models/Approval.interface';

import styles from './list.module.scss';
import { StoreNames, useAppStore } from 'stores';

interface Props {
  disabled: boolean;
}

export const ApprovalFilter: FC<Props> = observer(({ disabled }) => {
  const { [StoreNames.approvalsStore]: approvalsStore } = useAppStore();

  const _setTransportType = (option: LabeledValue<TransportTypeEnum>): void => {
    approvalsStore.setFiltersTransportType(option.value);
  };

  const options = approvalsStore.filter === 'active' ? activeOptions : closedOptions;

  return (
    <div className={styles.filters}>
      <Select
        labelInValue
        disabled={disabled}
        value={approvalsStore.filtersSettings.transportType as unknown as LabeledValue<TransportTypeEnum>}
        onChange={_setTransportType}
        options={TransportTypeOptions}
        style={{ width: '100%' }}
        onClear={() => approvalsStore.setFiltersTransportType(undefined)}
        allowClear
      />
      <Select
        disabled={disabled}
        value={approvalsStore.filtersSettings.statusSettings}
        onChange={value => {
          approvalsStore.setFiltersStatusSettings(value);
        }}
        allowClear
        style={{ width: '100%', marginTop: '16px' }}
      >
        {options.map(option => (
          <Select.Option key={option.value} value={option.value}>
            {option.label}
          </Select.Option>
        ))}
      </Select>
    </div>
  );
});
