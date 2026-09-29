/* eslint-disable @typescript-eslint/no-explicit-any */
import { IEmployeeStore, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { Form, Modal } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';

import { ValidationRules, validationPatterns } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useModalState } from 'shared/hooks/useModal';
import TButton from 'shared/ui/Button/Button';

interface Props {
  selfStore: ISelfEmployeeStore;
  empStore: IEmployeeStore;
}

export const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

export const PhoneModal: FC<Props> = observer(({ selfStore, empStore }) => {
  const { editPhone } = empStore;
  const { updatePhoneStatus } = selfStore;

  const [modalVisibility, modalActions] = useModalState(false);

  const [form] = Form.useForm();

  useEffect(() => {
    if (selfStore.selfEmployee.mobilePhone.length === 0) {
      modalActions.show();
    } else {
      modalActions.hide();
    }
  }, [modalActions, selfStore.isRequiredPhone, selfStore.selfEmployee.mobilePhone.length]);

  const onFinish = (values: any): void => {
    const mobilePhone = clearPhone(values.mobilePhone) ?? '';

    if (mobilePhone) {
      editPhone(mobilePhone);

      if (mobilePhone) {
        updatePhoneStatus(!selfStore.isRequiredPhone);
      }
    }

    modalActions.hide();
  };

  return (
    <Modal
      title={null}
      footer={null}
      visible={modalVisibility}
      centered={true}
      onCancel={() => {
        modalActions.hide();
        updatePhoneStatus(!selfStore.isRequiredPhone);
      }}
      destroyOnClose={true}
    >
      <>
        <div title="Введите номер телефона" />
        <div>
          <h4>Введите номер телефона</h4>
          <Form
            name="phone-form"
            form={form}
            onFinish={onFinish}
            preserve={false}
          >
            <FormField
              name="mobilePhone"
              placeholder="Телефон"
              type={FieldType.phone}
              rules={[
                ValidationRules.general.required,
                {
                  pattern: validationPatterns.numberValidation,
                  message: 'Неправильный формат телефона',
                },
              ]}
            />
            <p style={{ color: '#909090', fontSize: 14 }}>
              Введите актуальный номер телефона, чтобы вы могли продолжить оформление заявки
            </p>
            <TButton
              htmlType="submit"
              $size="small"
              style={{
                border: 'none',
                backgroundColor: '#F2F3F6',
                color: '#4D4D4D',
                borderColor: '#F2F3F6',
              }}
            >
              Продолжить
            </TButton>
          </Form>
        </div>
      </>
    </Modal>
  );
});
