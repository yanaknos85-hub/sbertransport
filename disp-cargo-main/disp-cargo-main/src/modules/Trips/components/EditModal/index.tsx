import React, { FC, useEffect, useState } from 'react';
import {
  Form, Modal, Spin, Input, DatePicker, InputNumber
} from 'antd';
import moment from 'moment';
import cn from 'classnames';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { StatusSelect } from 'components/StatusSelect';
import styles from './index.module.scss';
import { useTripsModal } from '../../context/TripsModal';
import { useValues } from './useValues';
import { useProfile } from 'api/profile/profile.api';
import { useEditCargoTrip } from 'api/trips-cargo/trips-cargo.api';
import { EditCargoTripData } from 'api/trips-cargo/trips-cargo.types';
import { ignore } from 'utils/utils';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { TRIP_STATUSES, TripStatuses } from 'constants/trips.constants';
import { DATE_FORMAT, TripTypes } from 'constants/app.constants';

export const EditModal: FC = () => {
  const { t } = useTranslation();
  const {
    isOpened, modalState, closeModal,
  } = useTripsModal();
  const [form] = useForm();
  const { contractorId } = useProfile().data;

  const { defaultValues } = useValues();

  const [editTrip, { isLoading }] = useEditCargoTrip(contractorId);

  const [driverId, setDriverId] = useState(defaultValues.driverId);

  const startTime = moment.parseZone(modalState.trip?.startTime);

  useEffect(() => {
    form.setFieldsValue(defaultValues);
    setDriverId(defaultValues.driverId);
  }, [defaultValues, form]);

  const handleCancel = () => {
    closeModal();
    setDriverId(defaultValues.driverId);
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleFinish = async (values: Store) => {
    if (!modalState.trip) {
      return;
    }

    const editData: EditCargoTripData = [];

    if (modalState.trip?.status !== values.status) {
      editData.push({
        field: 'status',
        value: values.status,
      });
    }

    const isTouched = (field: keyof typeof modalState.trip) => modalState.trip?.[field] ? form.isFieldTouched(field) : !!values[field] && values[field] !== '0';

    if (isTouched('loadersWorkTime')) {
      editData.push({
        field: 'loadersWorkTime',
        value: +values.loadersWorkTime * 60 * 1000,
      });
    }

    if (isTouched('factDistance')) {
      editData.push({
        field: 'factDistance',
        value: values.factDistance ? +String(values.factDistance)?.replace?.(',', '.') : 0,
      });
    }

    if (isTouched('factCost')) {
      editData.push({
        field: 'factCost',
        value: values.factCost ? +String(values.factCost * 100)?.replace?.(',', '.') : 0,
      });
    }

    if (isTouched('dispatcherStartTime') && values.dispatcherStartTime) {
      editData.push({
        field: 'dispatcherStartTime',
        value: values.dispatcherStartTime?.format('YYYY-MM-DDTHH:mm:ssZ'),
      });
    }

    if (isTouched('finishTime') && values.finishTime) {
      editData.push({
        field: 'finishTime',
        value: values.finishTime?.format('YYYY-MM-DDTHH:mm:ssZ'),
      });
    }

    if (isTouched('loaders')) {
      editData.push({
        field: 'loaders',
        value: +values.loaders,
      });
    }

    if (isTouched('driverWaitingTime')) {
      editData.push({
        field: 'driverWaitingTime',
        value: +values.driverWaitingTime * 60 * 1000,
      });
    }

    editTrip({
      tripId: modalState.trip.id,
      data: editData,
    })
      .then(closeModal)
      .catch(ignore);
  };

  return (
    <Modal
      title={`${t.Requests.Modal.EditTrip.title} ${modalState.trip?.humanReadableId ?? ''}`}
      className={styles.modal}
      visible={isOpened('editTrip')}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={t.global.save}
      okButtonProps={{ loading: isLoading }}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
      afterClose={form.resetFields}
      destroyOnClose
    >
      <Spin spinning={isLoading}>
        <Form
          form={form}
          onFinish={handleFinish}
          initialValues={defaultValues}
          className={styles.customFormErrors}
        >
          {driverId === modalState.trip?.driver?.id && (
            <Form.Item
              label={t.Requests.Modal.SetDriver.status}
              name="status"
              rules={[ValidationRules.general.required]}
            >
              {modalState.trip?.status && (
                <StatusSelect
                  disabled={TripStatuses[modalState.trip?.status].isFinal}
                  isEdit
                  tripMode={TripTypes.Cargo}
                  data-name="platform_inner_status_route"
                />
              )}
            </Form.Item>
          )}
          <Form.Item
            label={(
              <span className={styles.subtitleModal}>
                {modalState.trip
                && [
                  TRIP_STATUSES.WAITING_FOR_ASSIGNMENT,
                  TRIP_STATUSES.DRIVER_ASSIGNED,
                  TRIP_STATUSES.ORDER_FINISHED,
                ].includes(modalState.trip?.status)
                  ? t.Requests.Modal.EditTrip.subtitle
                  : ''}
              </span>
            )}
          >
            <div className={styles.grid}>
              {modalState.trip
              && [TRIP_STATUSES.WAITING_FOR_ASSIGNMENT, TRIP_STATUSES.DRIVER_ASSIGNED].includes(
                modalState.trip?.status
              ) && (
              <>
                <Form.Item label={t.Requests.Modal.EditTrip.dispatcherStartTime} name="dispatcherStartTime">
                  <DatePicker
                    className={styles.input}
                    allowClear
                    showTime={{ format: 'HH:mm' }}
                    showSecond={false}
                    format={DATE_FORMAT.DATE_WITH_TIME}
                  />
                </Form.Item>
                <Form.Item label={t.Requests.Modal.EditTrip.loaders} name="loaders">
                  <InputNumber
                    className={cn(styles.input, styles.inputNumber)}
                    min={1}
                    max={9}
                    maxLength={1}
                    disabled={form.getFieldValue('loaders') === 0}
                    onKeyPress={e => {
                      const charCode = e.key;
                      if (isNaN(parseInt(charCode, 10))) {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </>
              )}
              {modalState.trip && TRIP_STATUSES.ORDER_FINISHED === modalState.trip?.status && (
                <>
                  <Form.Item label={t.Requests.Modal.EditTrip.factDistance} name="factDistance">
                    <Input className={styles.input} maxLength={30} />
                  </Form.Item>
                  <Form.Item
                    label={t.Requests.Modal.EditTrip.driverWaitingTime}
                    name="driverWaitingTime"
                    rules={[
                      ValidationRules.general.maxWaitingTime(),
                    ]}
                  >
                    <Input
                      className={styles.input}
                      maxLength={30}
                      onKeyPress={e => {
                        const charCode = e.key;
                        if (isNaN(parseInt(charCode, 10))) {
                          e.preventDefault();
                        }
                      }}
                    />
                  </Form.Item>
                  <Form.Item
                    label={t.Requests.Modal.EditTrip.loadersWorkTime}
                    name="loadersWorkTime"
                    rules={[ValidationRules.general.maxLoadersTime()]}
                  >
                    <Input
                      className={styles.input}
                      maxLength={30}
                      onKeyPress={e => {
                        const charCode = e.key;
                        if (isNaN(parseInt(charCode, 10))) {
                          e.preventDefault();
                        }
                      }}
                    />
                  </Form.Item>
                  <Form.Item
                    label={t.Requests.Modal.EditTrip.factCost}
                    name="factCost"
                    rules={[
                      ValidationRules.general.floatTwoSymbols(),
                    ]}
                  >
                    <Input className={styles.input} maxLength={10} />
                  </Form.Item>
                  <Form.Item label={t.Requests.Modal.EditTrip.finishTime} name="finishTime">
                    <DatePicker
                      className={styles.input}
                      allowClear
                      showTime={{ format: 'HH:mm' }}
                      showSecond={false}
                      format={DATE_FORMAT.DATE_WITH_TIME}
                      disabledDate={current => current.isBefore(startTime)}
                    />
                  </Form.Item>
                </>
              )}
            </div>
          </Form.Item>
        </Form>
      </Spin>
    </Modal>
  );
};
