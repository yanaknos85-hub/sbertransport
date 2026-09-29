import React, { FC, useEffect, useState } from 'react';
import { Form, Modal, Spin } from 'antd';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { UUID } from 'utils/io-ts';
import moment from 'moment';
import { SelectCargoAvaliableDrivers } from 'components/SelectCargoAvaliableDrivers';
import { useTripsModal } from '../../context/TripsModal';
import styles from './index.module.scss';
import { useValues } from './useValues';
import DriverTypeSwitcher, { DriverType } from 'components/DriverTypeSwitcher/DriverTypeSwitcher';
import ErrorBoundary from 'components/ErrorBoundary';
import { useProfile } from 'api/profile/profile.api';
import { useSetDriverToCargoRequest } from 'api/trips-cargo/trips-cargo.api';
import { ignore, preventDefault } from 'utils/utils';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { DATE_FORMAT } from 'constants/app.constants';

export const SetDriverModal: FC = () => {
  const { t } = useTranslation();
  const {
    isOpened, modalState, closeModal,
  } = useTripsModal();
  const [form] = useForm();
  const { contractorId } = useProfile().data;
  const [setDriver, { isLoading }] = useSetDriverToCargoRequest(contractorId);
  const { defaultValues } = useValues();

  const [driverType, setDriverType] = useState<DriverType>('online');

  useEffect(() => {
    setDriverType('online');
  }, [modalState.type]);

  useEffect(() => {
    form.setFieldsValue(defaultValues);
  }, [defaultValues, form]);

  const handleCancel = () => {
    closeModal();
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleSetDriver = (values: Store) => {
    setDriver({
      tripId: modalState.trip?.id as UUID, driverId: values.driverId, planningShiftId: values.planningShiftId,
    })
      .then(closeModal)
      .catch(ignore);
  };

  return (
    <ErrorBoundary>
      <Modal
        title={`Поездка ${modalState.trip?.humanReadableId ?? ''}`}
        className={styles.modal}
        visible={isOpened('setDriver')}
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
                <SelectCargoAvaliableDrivers
                  longitude={modalState.trip?.waypoints?.[0].longitude ?? 0.0}
                  latitude={modalState.trip?.waypoints?.[0].latitude ?? 0.0}
                  deadline={moment(modalState.trip?.dispatcherStartTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO)}
                  onInputKeyDown={preventDefault}
                  defaultSelectedDriver={modalState.trip?.driver}
                />
              </Form.Item>
            )}
            {driverType === 'all' && (
              <Form.Item
                label={t.Requests.Modal.SetDriver.driver}
                name="planningShiftId"
                rules={[ValidationRules.general.required]}
              >
                <SelectCargoAvaliableDrivers
                  longitude={modalState.trip?.waypoints?.[0].longitude ?? 0.0}
                  latitude={modalState.trip?.waypoints?.[0].latitude ?? 0.0}
                  deadline={moment(modalState.trip?.startTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO)}
                  onInputKeyDown={preventDefault}
                  defaultSelectedDriver={modalState.trip?.driver ?? modalState.trip?.planned?.driver}
                  forPlanning
                />
              </Form.Item>
            )}
          </Form>
        </Spin>
      </Modal>
    </ErrorBoundary>
  );
};
