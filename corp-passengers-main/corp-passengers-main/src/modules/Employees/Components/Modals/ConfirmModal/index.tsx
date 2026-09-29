import React, { useState } from 'react';
import { SaveOutlined } from '@ant-design/icons';
import {
  Button, Form, Modal, Input
} from 'antd';

import { useModalState } from 'shared/hooks/useModal';
import { useResetUserPass } from 'api/reset-pass';
import { OrgStructureType } from 'constants/constants.app';
import { Employee } from 'stores/Employee/Employee.interface';
import { preventDefault } from 'utils';

export const ConfirmModal: React.FC<{ employee: Employee }> = ({ employee }): JSX.Element => {
  const [form] = Form.useForm();
  const [modalVisibility, modalActions] = useModalState();
  const [controlWord, setControlWord] = useState('');
  const [resetUserPassword] = useResetUserPass();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => setControlWord(event.target.value);
  const handleFinish = () => resetUserPassword({ userId: employee.id });

  // TODO secret word is hard coded! It's not best practise;
  const checkControlWord = (value: string): boolean => value !== 'Подтверждаю' && value !== 'подтверждаю';

  return (
    <>
      {employee.orgStructureType === OrgStructureType.EXTERNAL && (
        <Form.Item>
          <Button
            icon={<SaveOutlined />}
            size="middle"
            onClick={modalActions.show}
          >
            Сбросить пароль
          </Button>
        </Form.Item>
      )}

      <Modal
        title="Подтверждение сброса пароля"
        visible={modalVisibility}
        onOk={(): void => {
          modalActions.hide();
          form.submit();
          form.resetFields(['controlWord']);
          setControlWord('');
        }}
        onCancel={(): void => {
          modalActions.hide();
          form.resetFields();
          form.resetFields(['controlWord']);
          setControlWord('');
        }}
        okText="Подтвердить"
        okButtonProps={{
          danger: true, disabled: checkControlWord(controlWord), htmlType: 'submit',
        }}
      >
        <Form
          form={form}
          layout="vertical"
          name="decline-request"
          size="middle"
          onFinish={handleFinish}
        >
          <Form.Item name="controlWord" label="Контрольное слово">
            <Input
              autoComplete="off"
              onChange={handleChange}
              onKeyDown={preventDefault}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};
