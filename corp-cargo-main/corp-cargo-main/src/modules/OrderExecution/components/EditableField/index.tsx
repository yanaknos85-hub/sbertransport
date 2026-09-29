import React, { useState, FC, useEffect } from 'react';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { notification } from 'antd';
import { useModalState } from 'shared/hooks/useModal';
import {
  useChangeRequestStatus,
} from 'api/trip-detailed-view';
import { useChangeCargoRequestStatus } from 'api/engineer';
import { STATUSES as CargoStatuses } from '../../constants/Cargo/Cargo';
import Select from '../Filter/Inputs/Select';
import { getAvailableStatuses } from 'modules/OrderExecution/utils';
import { ChangedFieldsProps, Fields } from 'modules/OrderExecution/types/types';
import { Source } from '../../interfaces/Orders.types';
import { Modal } from 'shared/components/Modal/Modal';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import styles from './styles.module.scss';

export const EditableField: FC<ChangedFieldsProps> = ({
  id,
  label,
  description,
  status,
  transportType,
  source,
  withAvailableFutureStatus,
  routeId,
  routeNumber,
}) => {
  const { t } = useTranslation();
  const [form] = useForm<Fields>();
  const { cargoStore } = useAppStoreContext();
  const [changeRequestStatus] = useChangeRequestStatus();
  const [changeCargoRequestStatus] = useChangeCargoRequestStatus();
  const STATUSES = [...CargoStatuses.slice(0, -1)];
  const [visible, { hide, show }] = useModalState();
  const [statusName, setStatusName] = useState<Fields>({ status: '' });
  const [statusOptions, setStatusOptions] = useState(getAvailableStatuses(transportType, status, STATUSES));

  useEffect(() => {
    if (!!routeId) {
      setStatusOptions([]);
    }
  }, [])

  const onSuccess = (nextStatus: string) => {
    if (withAvailableFutureStatus) {
      const idx = statusOptions.findIndex(({ value }) => value === nextStatus);
      idx !== -1 && setStatusOptions(statusOptions.slice(idx));
    }
  };

  const handleSubmit = (value: Fields) => {
    if (!value.status || value.status === status) return;

    if (value.status.startsWith('CARGO_')) {
      changeCargoRequestStatus({
        requestId: id,
        status: value.status,
        source: source === Source.HOME_CLICK ? Source.HOME_CLICK : Source.WEB,
      })
      .then(() => {
        onSuccess(value.status)
        cargoStore.getCargoOrderListDeferredPost();
        hide();
      });
      return;
    }

    changeRequestStatus({ requestId: id, status: value.status })
      .then(() => {
        onSuccess(value.status);
        hide();
      });
  };

  return (
    <>
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
              onClick={() => {
                if (!!routeId) {
                  notification.error({
                    message: 'Внимание!',
                    description: `
                      Заявка включена в маршрут ${routeNumber}.
                      Статус заявки  не может быть изменён.
                      Для изменения статуса заявок перейдите в журнал маршрутов.`
                  });
                }
              }}
              onChange={(value: string) => {
                if (!routeId) {
                  show();
                }
                setStatusName({ status: value });
              }}
            />
          </Form.Item>
        </div>
      </Form>
      <Modal
        visible={visible}
        onOk={() => handleSubmit(statusName)}
        onCancel={() => {
          form.setFieldsValue({ status: description as string });
          hide();
        }}
      >
        <p>
          <b>Пожалуйста, подтвердите свое действие.</b>
        </p>
        <p>
          Вы пытаетесь изменить статус заявки, которая не была включена в маршрут.
          Продолжение может привести к нарушению установленного бизнес-процесса.
        </p>
      </Modal>
    </>
  );
};
