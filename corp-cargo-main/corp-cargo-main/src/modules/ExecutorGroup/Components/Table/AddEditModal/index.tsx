/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC, useCallback } from 'react';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import {
  Form, Spin
} from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { useTranslation } from 'i18n';
import { Button } from 'shared/components/Button/Button';
import { useFields } from './hooks/useFields';
import { useModalForm } from './hooks/useModalForm';
import { StyledFlex, StyledTitle, StyledBlockTitle } from '../styled.table';
import { useModal } from '../context/modal.context';
import { useGetExecutorGroup } from 'api/executor-group';

export const AddEditModal: FC = () => {
  const { t } = useTranslation();
  const { modalState, closeModal } = useModal();

  const { data, isLoading } = useGetExecutorGroup(
    modalState.type === 'edit' ? modalState.id as UUID : undefined
  );

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    execOrgId,
    setExecOrgId,
    orgIds,
    setOrgIds,
    initialValues, // дефолтные значения
    // @ts-ignore
  } = useModalForm(data?.data);

  const {
    parameterFields,
    customerFields,
    localDepartments,
  } = useFields(
    modalState.type === 'add',
    execOrgId as UUID,
    orgIds as UUID[],
    modalState.type === 'add' || modalState.type === 'edit',
    initialValues?.executors as UUID[],
    initialValues?.departments as UUID[]
  );

  const handleSaveForm = useCallback(async (values: any) => {
    const formData = {
      ...values,
      departments: localDepartments,
    };

    await saveForm(formData);
  }, [saveForm, localDepartments]);

  const handleOrgsChange = useCallback((event: any) => {
    const val = Array.isArray(event) ? event : event?.target?.value ?? [];
    setOrgIds(val as UUID[]);
    form.setFieldsValue({ departments: [] });
  }, [form, setOrgIds]);

  const handleExecOrgChange = useCallback((event: any) => {
    const val = event?.target?.value ?? event;
    setExecOrgId(val as UUID);
    form.resetFields(['executors']);
  }, [form, setExecOrgId]);

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={closeModal}
      onOk={form.submit}
      width={908}
      footer={[
        <Button
          type="text"
          danger
          onClick={closeModal}
          key="cancel"
        >
          {t.global.cancel}
        </Button>,
        <Button
          type="primary"
          onClick={form.submit}
          key="save"
        >
          {modalState.type === 'add' ? t.global.create : t.global.save}
        </Button>,
      ]}
    >
      <StyledTitle>{modalState.type === 'add' ? 'Новая группа исполнителей' : 'Редактирование'}</StyledTitle>
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={handleSaveForm}
      >
        <Spin spinning={isLoading}>
          <StyledFlex>
            <StyledBlockTitle>Параметры группы</StyledBlockTitle>
            {parameterFields.map(field => {
              return field.name === 'organizationId'
                ? (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                    onChange={handleExecOrgChange}
                  />
                )
                : (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                );
            })}
            <StyledBlockTitle>Настройки правил по заказчику</StyledBlockTitle>
            {customerFields.map(field => {
              return field.name === 'organizations'
                ? (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                    onChange={handleOrgsChange}
                  />
                )
                : (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                );
            })}
          </StyledFlex>
        </Spin>
      </Form>
    </Modal>
  );
};
