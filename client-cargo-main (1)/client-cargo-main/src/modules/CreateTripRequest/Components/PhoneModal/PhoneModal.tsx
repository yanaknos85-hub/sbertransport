import React, { FC, useEffect } from 'react';
import { EmployeeModel, IEmployeeStore, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { Form, Modal } from 'antd';
import { observer } from 'mobx-react';
import { validationPatterns, ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useModalState } from 'shared/hooks/useModal';
import TButton from 'shared/ui/Button/Button';

import { HeaderComponent } from 'modules/Evaluation/Components/Header/HeaderComponent';
import { ReactComponent as CloseIcon } from 'modules/Evaluation/static/icons/closeIcon.svg';
import { ButtonBlock, CommentBlock } from 'modules/Evaluation/static/styles';

interface Props {
  selfStore: ISelfEmployeeStore;
  empStore: IEmployeeStore;
}

export const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

export const PhoneModal: FC<Props> = observer(({ selfStore, empStore }) => {
  const { editEmployee } = empStore;
  const { selfEmployee, updatePhoneStatus } = selfStore;

  const [modalVisibility, modalActions] = useModalState(false);

  const [form] = Form.useForm();

  useEffect(() => {
    if (selfStore.isRequiredPhone && selfStore.selfEmployee.mobilePhone.length === 0) {
      modalActions.show();
    }
  }, [modalActions, selfStore.isRequiredPhone, selfStore.selfEmployee.mobilePhone.length]);

  const onFinish = (values: any): void => {
    const model = new EmployeeModel({
      ...selfEmployee,
      mobilePhone: clearPhone(values.mobilePhone) ?? '',
    });

    if (model) {
      editEmployee(model);
    }

    modalActions.hide();
  };

  return (
    <Modal
      title={null}
      footer={null}
      open={modalVisibility}
      centered={true}
      closeIcon={<CloseIcon />}
      onCancel={() => {
        modalActions.hide();
        updatePhoneStatus(!selfStore.isRequiredPhone);
      }}
      destroyOnClose={true}
    >
      <>
        <HeaderComponent title="Введите номер телефона" />
        <CommentBlock>
          <h4>Введите номер телефона</h4>
          <Form
            name="complaint-form"
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
            <ButtonBlock>
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
            </ButtonBlock>
          </Form>
        </CommentBlock>
      </>
    </Modal>
  );
});
