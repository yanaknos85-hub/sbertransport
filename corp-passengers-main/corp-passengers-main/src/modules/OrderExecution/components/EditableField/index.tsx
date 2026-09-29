import React, { useState, FC, useEffect } from 'react';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';

import {
  useChangeRequestStatus,
  useDriverAssignedStatusWithCarInfo,
  useStartPersonalTrip
} from 'api/trip-detailed-view';
import { useChangeCargoRequestStatus } from 'api/engineer';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { STATUSES } from '../../constants/Statuses';

import Select from '../Filter/Inputs/Select';
import { getAvailableStatuses } from 'modules/TripDetailed/utils';
import { ChangedFieldsProps, Fields } from 'modules/TripDetailed/types/types';
import { TripInProgressStatuses } from 'modules/TripDetailed/DetailedViewStatuses';
import { Source } from '../../interfaces/Orders.types';
import { useApprovalSettings } from 'api/approval-settings';
import { useProfile } from 'api/profile';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

import styles from './styles.module.scss';

export const EditableField: FC<ChangedFieldsProps> = ({
  id,
  label,
  description,
  status,
  transportType,
  departureAddressCoordinates,
  source,
  withAvailableFutureStatus,
}) => {
  const { t } = useTranslation();
  const [form] = useForm<Fields>();
  const { passengerStore } = useAppStoreContext();

  const [changeRequestStatus] = useChangeRequestStatus();
  const [changeCargoRequestStatus] = useChangeCargoRequestStatus();
  const [driverAssignedStatusWithCarInfo] = useDriverAssignedStatusWithCarInfo();
  const [startPersonalTrip] = useStartPersonalTrip();
  const { data: profile } = useProfile();
  const {
    refetch,
    data: approvalSettings,
  } = useApprovalSettings(profile.organizationId, transportType as TransportTypes);
  const [statusOptions, setStatusOptions] = useState<{ label: string; value: string }[]>();

  useEffect(() => {
    approvalSettings && setStatusOptions(getAvailableStatuses(
      transportType,
      status,
      STATUSES,
      approvalSettings?.approvalSettings
    ));
  }, [approvalSettings, transportType]);

  const onSuccess = (nextStatus: string) => {
    if (withAvailableFutureStatus && statusOptions) {
      const idx = statusOptions.findIndex(({ value }) => value === nextStatus);
      idx !== -1 && setStatusOptions(statusOptions.slice(idx));
    }
  };

  const handleSubmit = (value: Fields) => {
    if (value.vehicleInfo) {
      driverAssignedStatusWithCarInfo({ requestId: id, vehicleInfo: { vehicleInfo: value.vehicleInfo } });
      return;
    }

    if (!value.status || value.status === status) return;

    const [, personalTripStart] = TripInProgressStatuses;

    if (value.status === personalTripStart) {
      startPersonalTrip({
        requestId: id,
        latitude: departureAddressCoordinates.latitude,
        longitude: departureAddressCoordinates.longitude,
      }).then(() => onSuccess(value.status));
      return;
    }

    if (value.status.startsWith('CARGO_')) {
      changeCargoRequestStatus({
        requestId: id,
        status: value.status,
        source: source === Source.HOME_CLICK ? Source.HOME_CLICK : Source.WEB,
      }).then(() => onSuccess(value.status));
      return;
    }

    changeRequestStatus({ requestId: id, status: value.status }).then(() => {
      onSuccess(value.status);

      if (profile.isOrganization) {
        passengerStore.getPersonOrderList();
      } else {
        passengerStore.getPersonOrderListExec();
      }
    });
  };

  useEffect(() => {
    refetch();
  }, [transportType, refetch]);

  return (
    <Form form={form}>
      <div className={styles.selectContainer}>
        <Form.Item
          label={label}
          name="status"
          initialValue={description}
        >
          <Select
            options={statusOptions}
            placeholder={t.DetailedView.DispatcherTrip.selectStatus}
            onChange={(value: string) => handleSubmit({ status: value })}
          />
        </Form.Item>
      </div>
    </Form>
  );
};
