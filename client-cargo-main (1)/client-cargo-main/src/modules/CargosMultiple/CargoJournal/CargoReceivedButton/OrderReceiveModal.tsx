import React, { FC, useState } from 'react';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { ValidationRules } from 'shared/fieldValidationRules';
import { Modal } from 'shared/form/CargoType/CargoType.style';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';

import { ReactComponent as CloseIcon } from '../../static/images/closeCross.svg';
import { RequestInfo } from './RequestInfo';

interface OrderReceiveModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess?: () => void;
  request: {
    requestId: string;
    data: (string | number)[];
    labels?: string[];
  };
}

export const OrderReceiveModal: FC<OrderReceiveModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  request,
}) => {
  const [form] = useForm();
  const [isLoading, setLoading] = useState(false);

  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();

  const defaultLabels = ['Номер заявки', 'Адрес доставки', 'Дата доставки'];
  const { requestId, data } = request;

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const { occupiedPlacesCountFact, comment } = values;

      setLoading(true);

      const success = await cargosStore.confirmationReceivingCargo(
        requestId,
        occupiedPlacesCountFact,
        comment
      );

      if (success) {
        onSuccess?.();
        onCancel?.();
        form.resetFields();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    onCancel?.();
    form.resetFields();
  };

  const field = {
    occupiedPlacesCountFact: {
      label: 'Количество',
      name: 'occupiedPlacesCountFact',
      type: FieldType.number,
      rules: [ValidationRules.general.required],
      params: {
        style: {
          width: '150px',
        },
      },
    },
    comment: {
      label: 'Комментарий',
      name: 'comment',
      type: FieldType.textarea,
      rules: [ValidationRules.general.maxLength(250)],
      params: {
        placeholder: 'Добавьте комментарий',
        maxLength: 250,
        autoSize: { minRows: 3, maxRows: 5 },
      },
    },
  };

  return (
    <Modal
      title="Подтверждение получения груза"
      okText="Подтвердить получение"
      cancelText="Отмена"
      open={visible}
      onCancel={handleCancel}
      confirmLoading={isLoading}
      closeIcon={<CloseIcon />}
      destroyOnClose
      footer={[
        <TButton
          $size="small"
          key="submit"
          onClick={onCancel}
        >
          Отмена
        </TButton>,
        <TButton
          $size="small"
          key="submit"
          onClick={handleOk}
        >
          Подтвердить получение
        </TButton>,
      ]}
    >
      <RequestInfo data={data} labels={defaultLabels} />
      <Form
        form={form}
        layout="vertical"
      >
        <FormField {...field.occupiedPlacesCountFact} />
        <FormField {...field.comment} />
      </Form>
    </Modal>
  );
};
