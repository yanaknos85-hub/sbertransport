import React, { FC } from 'react';
import { Modal } from 'antd';
import Form, { FormInstance } from 'antd/es/form/Form';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import { HeaderComponent } from './Components/Header/HeaderComponent';
import { Success } from './Components/Success/Success';
import { ReactComponent as CloseIcon } from './static/icons/closeIcon.svg';
import { ButtonBlock, CommentBlock } from './static/styles';

interface Props {
  request: CargoRequestModel;
  complaintVisible: boolean;
  onCancel: () => void;
  onSubmit: (values: any) => void;
  requestStatus: number | undefined;
  form: FormInstance;
}

export const ComplaintModal: FC<Props> = ({
  request, complaintVisible, onCancel, onSubmit, requestStatus, form,
}) => (
  <Modal
    title={null}
    footer={null}
    open={complaintVisible}
    centered={true}
    closeIcon={<CloseIcon />}
    onCancel={onCancel}
    closable={!requestStatus}
    destroyOnClose={true}
  >
    {requestStatus && requestStatus === 200 ? (
      <Success
        title="Спасибо за обратную связь"
        text="Мы обязательно свяжемся с Вами и решим проблему. Информируем, что жалоба не является инструментом ускорения действий по процессу"
      />
    ) : (
      <>
        <HeaderComponent
          title={`Посылка ${request.humanReadableId}`}
          subTitle="Пожалуйста, опишите подробнее Вашу жалобу"
        />
        <CommentBlock>
          <h4>Комментарий</h4>
          <Form
            name="complaint-form"
            form={form}
            onFinish={onSubmit}
            preserve={false}
          >
            <FormField
              name="complaintComment"
              placeholder="Оставьте комментарий"
              type={FieldType.textarea}
              rules={[ValidationRules.general.maxLength(180)]}
              params={{ maxLength: 180, rows: 4 }}
            />
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
                Пожаловаться
              </TButton>
            </ButtonBlock>
          </Form>
        </CommentBlock>
      </>
    )}
  </Modal>
);
