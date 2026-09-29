import {
  Form, Modal, Spin
} from 'antd';
import React, { FC, useEffect, useState } from 'react';
import { useTranslation } from 'i18n';
import { SelectAvaliableDrivers } from 'components/SelectAvaliableDrivers';
import DriverTypeSwitcher, { DriverType } from 'components/DriverTypeSwitcher/DriverTypeSwitcher';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import moment from 'moment';
import { StatusSelect } from 'components/StatusSelect';
import styles from './index.module.scss';
import { useTripsModal } from '../../context/TripsModal';
import { useValues } from './useValues';
import { UUID } from 'utils/io-ts';
import { useProfile } from 'api/profile/profile.api';
import { useEditTrip } from 'api/trips/trips.api';
import { EditTripData } from 'api/trips/trips.types';
import { ignore, preventDefault } from 'utils/utils';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { DATE_FORMAT, TripTypes } from 'constants/app.constants';
import { TripStatuses } from 'constants/trips.constants';

export const EditModal: FC = () => {
  const { t } = useTranslation();
  const { modalState, closeModal } = useTripsModal();
  const [form] = useForm();
  const { contractorId } = useProfile().data;

  const { defaultValues } = useValues();

  const [editTrip, { isLoading }] = useEditTrip(contractorId);

  const [driverId, setDriverId] = useState(defaultValues.driverId);
  const [planningShiftId, setPlanningShiftId] = useState(defaultValues.planned?.driver.shiftId);
  const [driverType, setDriverType] = useState<DriverType>('free');

  useEffect(() => {
    form.setFieldsValue(defaultValues);
  }, [defaultValues, form]);

  useEffect(() => {
    form.setFieldsValue({
      driverId: defaultValues.driverId,
      planningShiftId: defaultValues.planningShiftId,
    });
    setDriverId(defaultValues.driverId);
    setPlanningShiftId(defaultValues.planningShiftId);
  }, [defaultValues, driverType, form]);

  useEffect(() => {
    setDriverType('free');
  }, [modalState.type]);

  const handleCancel = () => {
    closeModal();
    setDriverId(defaultValues.driverId);
    setPlanningShiftId(defaultValues.planningShiftId);
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleFinish = async (values: Store) => {
    if (!modalState.trip) {
      return;
    }

    const editData: EditTripData = [];

    if (
      ((driverType === 'online' || driverType === 'free') && modalState.trip?.driver?.id !== values.driverId)
      || (driverType === 'all' && modalState.trip.planned?.driver.shiftId !== values.planningShiftId)
    ) {
      editData.push({
        field: driverType === 'online' || driverType === 'free' ? 'driverId' : 'planningShiftId',
        value: driverType === 'online' || driverType === 'free' ? values.driverId : values.planningShiftId,
      });
    } else if (modalState.trip?.status !== values.status) {
      editData.push({
        field: 'status',
        value: values.status,
      });
    }

    editTrip({
      tripId: modalState.trip.id,
      data: editData,
    })
      .then(closeModal)
      .catch(ignore);
  };

  const isDriverChanged = driverId !== modalState.trip?.driver?.id
    || planningShiftId !== modalState.trip?.planned?.driver.shiftId;

  return (
    <Modal
      title={`${t.Requests.Modal.EditTrip.title} ${modalState.trip?.humanReadableId ?? ''}`}
      className={styles.modal}
      visible={modalState.type === 'editTrip'}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={t.global.save}
      cancelText={t.global.cancel}
      okButtonProps={{ loading: isLoading }}
      cancelButtonProps={{ className: styles.cancelButton }}
      afterClose={form.resetFields}
      destroyOnClose
    >
      <Spin spinning={isLoading}>
        <Form
          form={form}
          onFinish={handleFinish}
          initialValues={defaultValues}
        >
          <Form.Item>
            <DriverTypeSwitcher driverType={driverType} setDriverType={setDriverType} />
          </Form.Item>
          {driverType === 'free' && (
            <Form.Item
              label={t.Requests.Modal.SetDriver.driver}
              name="driverId"
              rules={[{ ...ValidationRules.general.required, required: !!modalState.trip?.driver }]}
            >
              <SelectAvaliableDrivers
                longitude={modalState.trip?.waypoints?.[0].longitude ?? 0.0}
                latitude={modalState.trip?.waypoints?.[0].latitude ?? 0.0}
                tripStartDate={
                  modalState.trip?.expectedStartTime && moment(modalState.trip?.expectedStartTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO)
                }
                onInputKeyDown={preventDefault}
                defaultSelectedDriver={modalState.trip?.driver}
                onChange={val => setDriverId(val as string)}
                allowClear={!modalState.trip?.driver}
                free
              />
            </Form.Item>
          )}
          {driverType === 'online' && (
            <Form.Item
              label={t.Requests.Modal.SetDriver.driver}
              name="driverId"
              rules={[{ ...ValidationRules.general.required, required: !!modalState.trip?.driver }]}
            >
              <SelectAvaliableDrivers
                longitude={modalState.trip?.waypoints?.[0].longitude ?? 0.0}
                latitude={modalState.trip?.waypoints?.[0].latitude ?? 0.0}
                tripStartDate={
                  modalState.trip?.expectedStartTime && moment(modalState.trip?.expectedStartTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO)
                }
                onInputKeyDown={preventDefault}
                defaultSelectedDriver={modalState.trip?.driver}
                onChange={val => setDriverId(val as string)}
                allowClear={!modalState.trip?.driver}
              />
            </Form.Item>
          )}
          {driverType === 'all' && (
            <Form.Item
              label={t.Requests.Modal.SetDriver.driver}
              name="planningShiftId"
              rules={[{ ...ValidationRules.general.required, required: !!modalState.trip?.planned }]}
            >
              <SelectAvaliableDrivers
                longitude={modalState.trip?.waypoints?.[0].longitude ?? 0.0}
                latitude={modalState.trip?.waypoints?.[0].latitude ?? 0.0}
                tripStartDate={
                  modalState.trip?.expectedStartTime && moment(modalState.trip?.expectedStartTime)
                    .utc().format(DATE_FORMAT.ISO)
                }
                tripEndDate={
                  modalState.trip?.expectedEndTime
                  && moment(modalState.trip.expectedEndTime).utc().format(DATE_FORMAT.ISO)
                }
                onInputKeyDown={preventDefault}
                defaultSelectedDriver={modalState.trip?.driver ?? modalState.trip?.planned?.driver}
                onChange={val => setPlanningShiftId(val as UUID)}
                allowClear={!modalState.trip?.planned}
                forPlanning
              />
            </Form.Item>
          )}
          {!isDriverChanged && (driverType === 'online' || driverType === 'free') && (
            <Form.Item
              label={t.Requests.Modal.SetDriver.status}
              name="status"
              rules={[ValidationRules.general.required]}
            >
              {modalState.trip?.status && (
                <StatusSelect
                  disabled={modalState.trip?.status && TripStatuses[modalState.trip.status].isFinal}
                  isEdit
                  tripMode={TripTypes.Passenger}
                />
              )}
            </Form.Item>
          )}
        </Form>
      </Spin>
    </Modal>
  );
};
