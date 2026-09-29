import React from 'react';
import { useTranslation } from 'i18n';
import { SelectProps } from 'antd/lib/select';
import { STATUSES } from 'modules/OrderExecution/constants/Statuses';
import Select from 'modules/OrderExecution/components/Filter/Inputs/Select';
import { getAvailableStatuses } from 'modules/TripDetailed/utils';
import { ChangedFieldsProps } from 'modules/TripDetailed/types/types';

import styles from './styles.module.scss';

export const StatusSelect: React.FC<Omit<ChangedFieldsProps, 'id' | 'label' | 'departureAddressCoordinates'>> = ({
  status,
  transportType,
  ...props
}) => {
  const { t } = useTranslation();

  const options = getAvailableStatuses(transportType, status, STATUSES);

  const statusValue = options.length
    ? (props as SelectProps<string>).value
    : STATUSES.find(({ name }) => name === status)?.rusName;

  return (
    <div className={styles.selectContainer}>
      <Select
        {...props}
        options={options}
        placeholder={t.DetailedView.DispatcherTrip.selectStatus}
        value={statusValue}
      />
    </div>
  );
};
