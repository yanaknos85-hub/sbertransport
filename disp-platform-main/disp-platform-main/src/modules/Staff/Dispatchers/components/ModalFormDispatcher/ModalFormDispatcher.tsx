import React, { FC, useEffect } from 'react';
import {
  Input, Form, Select, Spin, Popconfirm
} from 'antd';
import { Store } from 'antd/es/form/interface';
import { useForm } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { Button, Modal } from '@sber-sbertransport/ui-kit/src';

import { useCreateDispatcher, useEditDispatcher } from 'api/dispatchers/dispatchers.api';
import { useProfile } from 'api/profile/profile.api';
import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';
import { useResetUserPass } from 'api/user/user.api';

import CustomInput from 'components/PhoneMask/inputMask';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { ignore, settingsPhoneNumber } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { preparePhoneForBackend } from 'utils/preparePhoneForBackend';

import { statuses } from '../../constants/statuses';
import { useModalForm } from '../../context/ModalForm';
import styles from './ModalFormDispatcher.module.scss';
import { useActiveDispatcher } from '../../context/ActiveDispatcher';

const {
  required, nameRule, patronymicRule, email, checkPhoneMask, minMaxLength,
} = ValidationRules.general;

export const ModalFormDispatcher: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();
  const { setActiveDispatcher } = useActiveDispatcher();
  const [form] = useForm();
  const { contractorId, autoparkId } = useProfile().data;
  const [createDispatcher, { isLoading }] = useCreateDispatcher({ contractorId });
  const [updateDispatcher, { isLoading: isUpdateLoading }] = useEditDispatcher({ contractorId });

  const isAdd = stateShowModal.type === 'add';

  const handleSuccess = () => {
    form.submit();
  };

  const handleCancel = () => {
    handleClose();
  };

  const handleSave = (values: ContractorDispatcher) => {
    const formattedDispatcher = {
      ...values,
      patronymic: values.patronymic || undefined,
      phone: preparePhoneForBackend(values.phone as string),
      autoparkId: isAdd ? autoparkId : stateShowModal.dispatcher.autoparkId,
    };
    const id = stateShowModal.dispatcher?.id as UUID;

    if (isAdd) {
      createDispatcher({ dispatcher: formattedDispatcher })
        .then(data => {
          data && setActiveDispatcher({ ...data.data });
          handleClose();
        })
        .catch(ignore);
      return;
    }

    updateDispatcher({ dispId: id, dispatcher: { ...formattedDispatcher, id } })
      .then(() => {
        setActiveDispatcher({ ...formattedDispatcher, id });
        handleClose();
      })
      .catch(ignore);
  };

  useEffect(() => {
    form.setFieldsValue(isAdd ? {} : (stateShowModal.dispatcher as Store));
  }, [stateShowModal, isAdd, form]);

  const [resetUserPass] = useResetUserPass();

  const handleResetPass = async () => {
    const id = stateShowModal.dispatcher?.id as UUID;
    await resetUserPass({ userId: id });
  };

  return (
    <Modal
      title={t.Registry.Modal[stateShowModal.type]}
      className={styles.modal}
      open={stateShowModal.isOpen}
      onCancel={handleCancel}
      afterClose={form.resetFields}
      footer={[
        !isAdd && (
          <Popconfirm
            placement="top"
            title={t.DispatcherForm.resetPass}
            onConfirm={handleResetPass}
            okText={t.global.reset}
            cancelText={t.global.cancel}
            key="resetPass"
          >
            <Button
              size="small"
              type="text"
              danger
            >
              {t.global.resetPass}
            </Button>
          </Popconfirm>
        ),
        <Button
          key="back"
          size="small"
          type="text"
          onClick={handleCancel}
          disabled={isLoading || isUpdateLoading}
        >
          {t.global.cancel}
        </Button>,
        <Button
          key="submit"
          type="primary"
          size="middle"
          onClick={handleSuccess}
          loading={isLoading || isUpdateLoading}
        >
          {isAdd ? t.global.add : t.global.save}
        </Button>,
      ]}
    >
      <Spin spinning={isLoading || isUpdateLoading}>
        <Form
          className={styles.filtersForm}
          form={form}
          onFinish={handleSave}
        >
          <Form.Item
            className={styles.formItem}
            name="lastName"
            label={t.Registry.Labels.lastName}
            rules={[required, nameRule, minMaxLength(1, 50)]}
          >
            <Input className={styles.input} />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="firstName"
            label={t.Registry.Labels.firstName}
            rules={[required, nameRule, minMaxLength(1, 50)]}
          >
            <Input className={styles.input} />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="patronymic"
            label={t.Registry.Labels.patronymic}
            rules={[patronymicRule, minMaxLength(1, 50)]}
          >
            <Input className={styles.input} />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="phone"
            label={t.Registry.Labels.phone}
            rules={[required, checkPhoneMask]}
          >
            <CustomInput className={styles.input} {...settingsPhoneNumber.input} />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="email"
            label={t.Registry.Labels.email}
            rules={[required, email, minMaxLength(5, 254)]}
          >
            <Input className={styles.input} maxLength={254} />
          </Form.Item>

          {!isAdd && (
            <Form.Item
              className={styles.formItem}
              name="status"
              label={t.Registry.Labels.status}
            >
              <Select
                disabled
                className={styles.select}
                options={statuses}
              />
            </Form.Item>
          )}
        </Form>
      </Spin>
    </Modal>
  );
};
