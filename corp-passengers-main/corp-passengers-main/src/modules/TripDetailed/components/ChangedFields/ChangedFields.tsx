/* eslint-disable jsx-a11y/label-has-for */
import { Button, Form, Select } from 'antd';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'i18n';
import {
  chooseCorrectStatusesList,
  useChangeRequestStatus,
  useDriverAssignedStatusWithCarInfo,
  useStartPersonalTrip
} from 'api/trip-detailed-view';
import { useForm } from 'antd/lib/form/Form';
import { useChangeCargoRequestStatus } from 'api/engineer';
import styles from '../../tripDetailView.module.scss';
import { getAvailableStatuses } from '../../utils';
import { VehicleInfoInput } from './VehicleInfo';
import { ChangedFieldsProps, Fields } from '../../types/types';
import {
  AwaitingApprovalStatuses,
  CanceledStatuses,
  FinalStatuses,
  FoundStatuses,
  TripInProgressStatuses
} from '../../DetailedViewStatuses';

export const ChangedFieldsForm: React.FC<ChangedFieldsProps> = ({
  id,
  label,
  description,
  status,
  transportType,
  departureAddressCoordinates,
}) => {
  const [isDisable, setIsDisable] = useState(true);
  const [isVisible, setIsVisible] = useState(false);
  const [isVehicleInfoVisible, setIsVehicleInfoVisible] = useState(false);
  // Todo закоментирован кусок кода - для статуса "Отменено" есть отдельная апишка, логика готова.
  // const [isCancelVisible, setIsCancelVisible] = useState(false);

  const { t } = useTranslation();
  const [form] = useForm<Fields>();

  const [changeRequestStatus] = useChangeRequestStatus();
  const [changeCargoRequestStatus] = useChangeCargoRequestStatus();
  // const [cancelStatusWithCancelCode] = useCancelStatusWithCancelCode();
  const [driverAssignedStatusWithCarInfo] = useDriverAssignedStatusWithCarInfo();
  const [startPersonalTrip] = useStartPersonalTrip();

  const { data: statuses } = chooseCorrectStatusesList(transportType)();

  const options = getAvailableStatuses(transportType, status, statuses);
  // const cancelRadioGroupOptions = getRadioGroupOptions(transportType);

  const handleSelectDropDown = (value: string) => {
    // Todo закоментирован кусок кода - для статуса "Отменено" есть отдельная апишка, логика готова.
    setIsDisable(value === status || !value);
    if (CanceledStatuses.includes(value)) {
      setIsVehicleInfoVisible(false);
      // setIsCancelVisible(true);
    } else if (FoundStatuses.includes(value)) {
      setIsVehicleInfoVisible(true);
      // setIsCancelVisible(false);
    } else {
      setIsVehicleInfoVisible(false);
      // setIsCancelVisible(false);
    }
  };

  const handleSubmit = (value: Fields) => {
    // Todo закоментирован кусок кода - для статуса "Отменено" есть отдельная апишка, логика готова.
    //  Ждём согласования от бизнеса изменения правил бэка - сейчас отменить может либо автор, либо пассажир.
    // if (!value.cancelCode && !value.vehicleInfo) {
    if (!value.vehicleInfo) {
      const [, personalTripStart] = TripInProgressStatuses;

      if (value.status === personalTripStart) {
        const startTripParams = {
          requestId: id,
          latitude: departureAddressCoordinates.latitude,
          longitude: departureAddressCoordinates.longitude,
        };

        startPersonalTrip(startTripParams);
        return;
      }
      //  У грузоперевозок свой endpoint по смене статусов, отправляем туда статусы с префиксом CARGO_
      if (value.status.startsWith('CARGO_')) {
        value.status && changeCargoRequestStatus({ requestId: id, status: value.status });
      } else {
        value.status && changeRequestStatus({ requestId: id, status: value.status });
      }
      return;
    }

    // if (value.cancelCode) {
    //   const [reason, code] = value.cancelCode.split(';');
    //   const cancelCode = { reason, code: Number(code) };
    //   cancelStatusWithCancelCode({ requestId: id, cancelCode });
    //   return
    // }
    if (value.vehicleInfo) {
      driverAssignedStatusWithCarInfo({ requestId: id, vehicleInfo: { vehicleInfo: value.vehicleInfo } });
    }
    // }
  };

  const handleReset = () => {
    setIsDisable(true);
    form.resetFields();
  };

  const showDescriptionItem = status && (AwaitingApprovalStatuses.includes(status) || FinalStatuses.includes(status));
  const descriptionLabel = label === '' ? '' : label.concat(':');

  useEffect(() => {
    setIsVisible(!showDescriptionItem);
  }, [showDescriptionItem]);

  return showDescriptionItem ? (
    <>
      <label className={styles.descriptionLabel}>{descriptionLabel}</label>
      <div>{description}</div>
    </>
  ) : (
    <Form
      form={form}
      onFinish={handleSubmit}
      className={styles.changeFieldsForm}
    >
      <div className={styles.selectContainer}>
        <Form.Item
          label={label}
          name="status"
          initialValue={description}
        >
          <Select
            className={styles.select}
            options={options}
            allowClear
            placeholder={t.DetailedView.DispatcherTrip.selectStatus}
            onChange={handleSelectDropDown}
          />
        </Form.Item>
        {isVehicleInfoVisible && <VehicleInfoInput />}
        {/* //Todo коды отмены готовы, сейчас отменить может либо автор, либо пассажир. */}
        {/* {isCancelVisible && <CancelCodes options={cancelRadioGroupOptions} />} */}
      </div>
      {isVisible && (
        <div className={styles.buttonBar}>
          <Button
            onClick={form.submit}
            type="primary"
            disabled={isDisable}
          >
            {`${t.DetailedView.DispatcherTrip.approve}`}
          </Button>
          <Button onClick={handleReset}>{`${t.DetailedView.DispatcherTrip.cancel}`}</Button>
        </div>
      )}
    </Form>
  );
};
