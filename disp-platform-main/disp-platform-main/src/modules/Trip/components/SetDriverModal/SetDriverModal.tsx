import { Form, Modal, Spin } from 'antd';
import { useTranslation } from 'i18n';
import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import styles from './setDriverModal.module.scss';
import { useForm } from 'antd/lib/form/Form';
import { UUID } from 'utils/io-ts';
import { SelectAvaliableDrivers } from 'components/SelectAvaliableDrivers';
import moment from 'moment';
import { ignore, preventDefault } from 'utils/utils';
import DriverTypeSwitcher, { DriverType } from 'components/DriverTypeSwitcher/DriverTypeSwitcher';
import { PassTrip } from 'api/trips/trips.types';
import { useProfile } from 'api/profile/profile.api';
import { useSetDriverToRequest } from 'api/trips/trips.api';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { DATE_FORMAT } from 'constants/app.constants';

interface SetDriverModalProps {
  trip: PassTrip;
  visible: boolean;
  closeModal: () => void;
}

export const SetDriverModal: FC<SetDriverModalProps> = ({
  trip, visible, closeModal,
}) => {
  const { t } = useTranslation();
  const [form] = useForm();
  const { contractorId } = useProfile().data;
  const [setDriver, { isLoading }] = useSetDriverToRequest(contractorId);
  const [driverType, setDriverType] = useState<DriverType>('online');

  useEffect(() => {
    setDriverType('online');
  }, [visible]);

  const defaultValues = useMemo(() => ({
    driverId: trip.driver?.id,
    planningShiftId: trip.planned?.driver.shiftId,
  }), [trip]);

  useEffect(() => {
    form.setFieldsValue(defaultValues);
  }, [defaultValues, form]);

  const handleCancel = () => {
    closeModal();
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleSetDriver = async (values: typeof defaultValues) => {
    setDriver({
      tripId: trip.id,
      driverId: values.driverId as UUID,
      planningShiftId: values.planningShiftId ?? undefined,
    })
      .then(closeModal)
      .catch(ignore);
  };

  return (
    <Modal
      title={`Поездка ${trip.humanReadableId}`}
      className={styles.modal}
      visible={visible}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={t.Requests.Modal.set}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
      afterClose={form.resetFields}
      destroyOnClose
    >
      <Spin spinning={isLoading}>
        <Form
          form={form}
          onFinish={handleSetDriver}
          initialValues={defaultValues}
        >
          <div className={styles.subtitle}>
            {driverType === 'online' && t.Requests.Modal.SetDriver.subtitle}
            {driverType === 'all' && t.Requests.Modal.SetDriver.subtitlePlanning}
          </div>

          <Form.Item>
            <DriverTypeSwitcher driverType={driverType} setDriverType={setDriverType} />
          </Form.Item>

          {driverType === 'online' && (
          <Form.Item
            label={t.Requests.Modal.SetDriver.driver}
            name="driverId"
            rules={[ValidationRules.general.required]}
          >
            <SelectAvaliableDrivers
              longitude={trip?.waypoints?.[0].longitude ?? 0.0}
              latitude={trip?.waypoints?.[0].latitude ?? 0.0}
              tripStartDate={moment(trip?.expectedStartTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO)}
              onInputKeyDown={preventDefault}
              defaultSelectedDriver={trip?.driver}
            />
          </Form.Item>
          )}

          {driverType === 'all' && (
          <Form.Item
            label={t.Requests.Modal.SetDriver.driver}
            name="planningShiftId"
            rules={[ValidationRules.general.required]}
          >
            <SelectAvaliableDrivers
              longitude={trip?.waypoints?.[0].longitude ?? 0.0}
              latitude={trip?.waypoints?.[0].latitude ?? 0.0}
              tripStartDate={moment(trip?.expectedStartTime).utc().format(DATE_FORMAT.ISO)}
              tripEndDate={moment(trip?.expectedEndTime).utc().format(DATE_FORMAT.ISO)}
              onInputKeyDown={preventDefault}
              defaultSelectedDriver={trip?.driver ?? trip.planned?.driver}
              forPlanning
            />
          </Form.Item>
          )}
        </Form>
      </Spin>
    </Modal>
  );
};
