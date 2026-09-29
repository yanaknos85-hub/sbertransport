import React, { FC, useState } from 'react';
import { Form, Modal } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { STATUSES as CargoStatuses, ChangedFieldsProps, Fields } from '../constants';
import Select from '../Select';
import { getAvailableStatuses } from './utils';
import { useUpdateRouteStatus } from 'api/planner';
import { RouteStatusEnum } from 'modules/Planner/types';
import styles from './styles.module.scss';

export const EditableField: FC<ChangedFieldsProps> = ({
  id, label, description, status,
}) => {
  const [form] = useForm<Fields>();
  const [updateRouteStatus] = useUpdateRouteStatus();
  const STATUSES = [...CargoStatuses];
  const [, setIsStatusUpdated] = useState(false);
  const options = getAvailableStatuses(status, STATUSES);

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [newStatus, setNewStatus] = useState<string | null>(null);
  const [currentStatus, setCurrentStatus] = useState<string | undefined>(status);

  const handleStatusChange = (value: string) => {
    if (currentStatus === RouteStatusEnum.CARGO_PLANNING_FINISHED) {
      setNewStatus(value);
      setIsModalVisible(true);
    } else {
      handleSubmit({ status: value });
    }
  };

  const handleSubmit = (value: Fields) => {
    if (!value.vehicleInfo && value.status.startsWith('CARGO_') && value.status !== currentStatus) {
      updateRouteStatus({ routelistId: id, status: value.status as RouteStatusEnum })
        .then(() => {
          setIsStatusUpdated(true);
          setCurrentStatus(value.status);
        });
    }
  };

  const handleOk = () => {
    if (newStatus) {
      handleSubmit({ status: newStatus });
    }
    setIsModalVisible(false);
    setNewStatus(null);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    setNewStatus(null);
    form.setFieldsValue({ status: currentStatus });
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
              options={options}
              placeholder=""
              onChange={handleStatusChange}
            />
          </Form.Item>
        </div>
      </Form>

      <Modal
        title="При изменении статусов в ручном режиме возможно нарушение работы автоматических функций"
        visible={isModalVisible}
        onOk={handleOk}
        onCancel={handleCancel}
      >
        <p>Вы точно хотите изменить статус?</p>
      </Modal>
    </>
  );
};
