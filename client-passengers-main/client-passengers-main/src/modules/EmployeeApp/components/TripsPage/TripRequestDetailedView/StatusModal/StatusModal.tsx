import {
  Button, Form, Modal, Select
} from 'antd';
import { RequestStatus } from 'api/trip-requests';
import { TTripRequestStatuses } from 'modules/EmployeeApp/TripRequestStatuses.constants';
import React, { Dispatch, SetStateAction, useState } from 'react';

import { useStatus } from './useStatus';

export type TripRequestStatusesToRus = { [T in TTripRequestStatuses]?: string };

export interface IRequestModalProps {
  statuses: RequestStatus[];
  activeStatus: string;
  handleSelect: (reqStatus: TTripRequestStatuses) => Promise<void>;
}

export interface IUseStatusProps {
  setVisible: Dispatch<SetStateAction<boolean>>;
}

export const StatusModal: React.FC<IRequestModalProps> = ({
  statuses, activeStatus, handleSelect,
}): JSX.Element => {
  const [form] = Form.useForm();
  const [visible, setVisible] = useState(false);
  const { showModal, hideModal } = useStatus({ setVisible });

  const handleOk = (): void => {
    const status: TTripRequestStatuses = form.getFieldValue('requestStatus');
    handleSelect(status).then(() => {
      hideModal();
    });
  };

  const options = Object.values(statuses).map(status => ({
    value: status.name,
    label: status.rusName,
  }));

  return (
    <div>
      <Button size="small" onClick={showModal}>
        {activeStatus}
      </Button>

      <Modal
        title="Изменение статуса поездки"
        visible={visible}
        onOk={handleOk}
        onCancel={hideModal}
        okText="Изменить статус"
        width={332}
        cancelButtonProps={{ style: { display: 'none' } }}
        centered
      >
        <Form form={form}>
          <Form.Item name="requestStatus" label="Статусы">
            <Select placeholder="Выберите статус" options={options} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
