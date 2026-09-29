import React, {
  FC, Suspense, useCallback, useEffect, useState
} from 'react';
import { DatePicker, Form, Modal } from 'antd';
import { FormInstance } from 'antd/lib/form';
import moment, { Moment } from 'moment';

import { useProfile } from 'api/profile/profile.api';
import {
  useCreateShifts, useShift, useUpdateShift
} from 'api/schedule2.0/schedule.api';
import { ShiftsUpdate } from 'api/schedule2.0/schedule.types';

import { TripTypes } from 'constants/app.constants';

import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';
import { getFullName } from 'utils/getFullName';

import { useTranslation } from 'i18n';

import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import { SelectDrivers } from 'components/SelectDrivers/SelectDrivers';
import { SelectVehicle } from '../../components/SelectVehicle';

import styles from './SimpleEditModal.module.scss';

/**
 * Внутренний компонент модалки: вся логика и форма редактирования смены.
 * Вынесен за Suspense, чтобы обёртка (Modal) рендерилась сразу,
 * а на время загрузки useShift внутри показывался спиннер.
 */
const SimpleEditModalForm: FC<{
  shiftId: UUID;
  form: FormInstance;
  onClose: () => void;
  onSubmitReady: (submit: () => void) => void;
  onLoadingChange: (loading: boolean) => void;
}> = ({
  shiftId,
  form,
  onClose,
  onSubmitReady,
  onLoadingChange,
}) => {
  const { t } = useTranslation();
  const { contractorId } = useProfile().data;

  // suspense позволяет обёртке показывать спиннер до загрузки данных смены
  const { data: shift } = useShift({ contractorId, shiftId }, { suspense: true });
  const [updateShift, { isLoading: isUpdating }] = useUpdateShift(contractorId, shiftId);
  const [createShift, { isLoading: isCreating }] = useCreateShifts(contractorId);

  useEffect(() => {
    if (!shift) return;

    form.setFieldsValue({
      vehicleId: shift.vehicle.id,
      driverId: shift.driver.id,
      startDate: moment.utc(shift.startDate).local(),
      endDate: shift.endDate ? moment.utc(shift.endDate).local() : undefined,
    });
  }, [shift, form]);

  // наружу отдаём функцию submit формы, чтобы внешняя модалка могла её вызвать из footer
  useEffect(() => {
    onSubmitReady(() => form.submit());
  }, [form, onSubmitReady]);

  useEffect(() => {
    onLoadingChange(isUpdating || isCreating);
  }, [isUpdating, isCreating, onLoadingChange]);

  const handleFinish = ({
    startDate, endDate, vehicleId, driverId,
  }: {
    startDate: Moment;
    endDate: Moment;
    vehicleId: UUID;
    driverId: UUID;
  }) => {
    if (!shift) return;

    const shiftData = {
      index: 0,
      driverId,
      vehicleId,
      startDate: startDate.clone().utc().startOf('minute').format(),
      endDate: endDate.clone().utc().endOf('minute').format(),
    };

    // смена из списка всегда имеет id -> обновляем её;
    // ветка создания оставлена на случай открытия без id
    if (shift.id) {
      const updatedValues: ShiftsUpdate = {
        ...shiftData,
        id: shift.id,
      };
      updateShift(updatedValues)
        .then(onClose)
        .catch(ignore);
    } else {
      createShift([shiftData])
        .then(onClose)
        .catch(ignore);
    }
  };

  return (
    <Form
      form={form}
      onFinish={handleFinish}
      layout="vertical"
      className={styles.form}
    >
      <Form.Item
        name="vehicleId"
        label={t.Shifts.vehicle}
        className={styles.formItem}
      >
        <SelectVehicle placeholder="Начните вводить" />
      </Form.Item>

      <Form.Item
        name="driverId"
        label={t.Shifts.driver}
        className={styles.formItem}
      >
        <SelectDrivers
          type={TripTypes.Universal}
          placeholder={shift?.driver ? getFullName(shift.driver) : 'Начните вводить'}
        />
      </Form.Item>

      <div className={styles.dates}>
        <Form.Item
          name="startDate"
          label={t.Shifts.startDate}
          rules={[{ required: true }]}
          className={styles.formItem}
        >
          <DatePicker showTime={{ format: 'HH:mm' }} format="DD.MM.YYYY HH:mm" />
        </Form.Item>

        <Form.Item
          name="endDate"
          label={t.Shifts.endDate}
          rules={[{ required: true }]}
          className={styles.formItem}
        >
          <DatePicker showTime={{ format: 'HH:mm' }} format="DD.MM.YYYY HH:mm" />
        </Form.Item>
      </div>
    </Form>
  );
};

/**
 * Обёртка: рендерит модалку сразу, а внутри — Suspense со спиннером
 * до тех пор, пока не загрузится смена через useShift.
 */
export const SimpleEditModal: FC<{
  shiftId: UUID;
  visible: boolean;
  onClose: () => void;
}> = ({
  shiftId, visible, onClose,
}) => {
  const { t } = useTranslation();

  const [form] = Form.useForm();
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  const [submit, setSubmit] = useState<() => void>(() => () => {});
  const [loading, setLoading] = useState(false);

  const handleSubmitReady = useCallback((fn: () => void) => {
    setSubmit(() => fn);
  }, []);

  const handleLoadingChange = useCallback((next: boolean) => {
    setLoading(next);
  }, []);

  return (
    <Modal
      className={styles.modal}
      width={600}
      title={t.Shifts.editShift}
      visible={visible}
      onCancel={onClose}
      okText={t.global.save}
      onOk={submit}
      okButtonProps={{ loading }}
      cancelText={t.global.cancel}
      destroyOnClose
    >
      <Suspense fallback={<SpinWrapped />}>
        <SimpleEditModalForm
          shiftId={shiftId}
          form={form}
          onClose={onClose}
          onSubmitReady={handleSubmitReady}
          onLoadingChange={handleLoadingChange}
        />
      </Suspense>
    </Modal>
  );
};
