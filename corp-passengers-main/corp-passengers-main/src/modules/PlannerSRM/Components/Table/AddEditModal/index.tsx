/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC } from 'react';
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
    orgId,
    setOrgId,
    depsId,
    setDepsId,
    initialValues, // дефолтные значения
    // @ts-ignore
  } = useModalForm(data?.data);

  const {
    parameterFields, customerFields,
  } = useFields(modalState.type === 'add', orgId as UUID[], depsId as UUID[], execOrgId as UUID);

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
        onFinish={saveForm}
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
                    onChange={val => {
                      setExecOrgId(val);
                      form.resetFields(['executors']);
                    }}
                  />
                )
                : (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                );
            }
            )}
            <StyledBlockTitle>Настройки правил по заказчику</StyledBlockTitle>
            {customerFields.map(field => {
              return field.name === 'organizations'
                ? (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                    onChange={val => {
                      setOrgId(val);
                      form.resetFields(['departments']);
                      form.resetFields(['customers']);
                    }}
                  />
                )
                : field.name === 'departments'
                  ? (
                    <ModelFormField
                      key={field.name}
                      {...field}
                      form={form}
                      onChange={val => {
                        setDepsId(val);
                        form.resetFields(['customers']);
                      }}
                    />
                  ) : (
                    <ModelFormField
                      key={field.name}
                      {...field}
                      form={form}
                    />
                  );
            }
            )}
          </StyledFlex>
        </Spin>
      </Form>
    </Modal>
  );
};
