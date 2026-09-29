import React, { FC, FormEventHandler, useEffect } from 'react';
import {
  Form, Modal, Spin
} from 'antd';
import { Input, InputNumber } from '@sber-sbertransport/ui-kit/src';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { ignore } from 'utils/utils';
import styles from './index.module.scss';
import { useValues } from './useValues';
import { EditTripData, PassTrip } from 'api/trips/trips.types';
import { useProfile } from 'api/profile/profile.api';
import { useEditTrip } from 'api/trips/trips.api';

interface EditModalProps {
  trip: PassTrip;
  visible: boolean;
  closeModal: () => void;
}

export const EditModal: FC<EditModalProps> = ({
  trip, visible, closeModal,
}) => {
  const { t } = useTranslation();

  const [form] = useForm();
  const { contractorId } = useProfile().data;

  const { defaultValues } = useValues(trip);

  const [editTrip, { isLoading }] = useEditTrip(contractorId);

  useEffect(() => {
    form.setFieldsValue(defaultValues);
  }, [defaultValues, form, visible]);

  const handleCancel = () => {
    closeModal();
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleFinish = (values: Store) => {
    const editData: EditTripData = [];

    const isTouched = (field: keyof typeof trip) => trip[field]
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      ? form.isFieldTouched(field as any)
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      : !!values[field as any] && values[field as any] !== '0';

    if (isTouched('driverWaitingTime')) {
      editData.push({
        field: 'driverWaitingTime',
        value: +values.driverWaitingTime * 60 * 1000,
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
        value: values.factCost ? +String(values.factCost)?.replace?.(',', '.') * 100 : 0,
      });
    }

    editTrip({
      tripId: trip.id,
      data: editData,
    })
      .then(closeModal)
      .catch(ignore);
  };

  const handleInputNumber: FormEventHandler<HTMLInputElement> = e => {
    // Оставляем только цифры, точку и запятую
    e.currentTarget.value = e.currentTarget.value?.replace(/[^0-9.,]/g, '');

    // Проверяем, есть ли уже точка или запятая
    let hasSeparator = false;
    e.currentTarget.value = e.currentTarget.value
      .split('')
      .map(char => {
        if (char === '.' || char === ',') {
          if (!hasSeparator) {
            hasSeparator = true;
            return char;
          } else {
            return ''; // Игнорируем дополнительные точки/запятые
          }
        }
        return char;
      })
      .join('');
  };

  return (
    <Modal
      title={`${t.Requests.Modal.EditTrip.title} ${trip.humanReadableId}`}
      className={styles.modal}
      visible={visible}
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
          <div className={styles.grid}>
            <Form.Item label={t.Requests.Modal.EditTrip.factDistance} name="factDistance">
              <InputNumber
                className={styles.input}
                maxLength={30}
                decimalSeparator=","
                onInputCapture={handleInputNumber}
                precision={2}
              />
            </Form.Item>
            <Form.Item label={t.Requests.Modal.EditTrip.driverWaitingTime} name="driverWaitingTime">
              <Input className={styles.input} maxLength={30} />
            </Form.Item>
            <Form.Item label={t.Requests.Modal.EditTrip.factCost} name="factCost">
              <InputNumber
                className={styles.input}
                maxLength={30}
                decimalSeparator=","
                onInputCapture={handleInputNumber}
                precision={2}
              />
            </Form.Item>
          </div>
        </Form>
      </Spin>
    </Modal>
  );
};
