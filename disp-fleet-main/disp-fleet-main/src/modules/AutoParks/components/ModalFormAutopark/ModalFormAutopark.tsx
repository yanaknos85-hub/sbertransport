import React, { FC, useEffect } from 'react';
import { Input, Modal, Form } from 'antd';
import { Store } from 'antd/es/form/interface';
import { useForm } from 'antd/lib/form/Form';

import { useCreateAutopark, useEditAutopark } from 'api/autopark/autopark.api';
import { AutoPark } from 'api/autopark/autopark.types';
import { useProfile } from 'api/profile/profile.api';
import { useTranslation } from 'i18n';

import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { UUID } from 'utils/io-ts';
import { useModalForm } from '../../context/ModalForm';
import styles from './ModalFormDispatcher.module.scss';

export const ModalFormAutopark: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();
  const [form] = useForm();
  const { contractorId } = useProfile().data;
  const [createAutopark] = useCreateAutopark({ contractorId });
  const [updateAutopark] = useEditAutopark({ contractorId });

  const isAdd = stateShowModal.type === 'add';

  const handleSuccess = () => {
    form.submit();
  };

  const handleCancel = () => {
    handleClose();
    form.resetFields();
  };

  const handleSave = (values: AutoPark) => {
    if (isAdd) {
      createAutopark({ autopark: values }).then(handleCancel);
      return;
    }

    const id = stateShowModal.autopark?.id as UUID;

    updateAutopark({ autoparkId: id, autopark: { ...values, id } }).then(handleCancel);
  };

  useEffect(() => {
    form.setFieldsValue(isAdd ? {} : (stateShowModal.autopark as Store));

    return () => form.resetFields();
  }, [stateShowModal, isAdd, form]);

  return (
    <Modal
      title={t.Autoparks.Modal[stateShowModal.type]}
      className={styles.modal}
      visible={stateShowModal.isOpen}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={isAdd ? t.global.add : t.global.save}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
    >
      <Form
        className={styles.filtersForm}
        form={form}
        onFinish={handleSave}
      >
        <Form.Item
          className={styles.formItem}
          name="name"
          label={t.Autoparks.Labels.name}
          rules={[ValidationRules.general.required, ValidationRules.general.checkTrimmedField()]}
        >
          <Input className={styles.input} maxLength={255} />
        </Form.Item>
      </Form>
    </Modal>
  );
};
