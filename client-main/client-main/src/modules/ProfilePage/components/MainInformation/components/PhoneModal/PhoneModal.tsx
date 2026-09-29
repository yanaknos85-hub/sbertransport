import { IEmployeeStore, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { Form, Modal } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { ValidationRules, validationPatterns } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import TButton from 'shared/ui/Button/Button';
import Close from 'shared/components/Images/Close.svg';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useCount from 'shared/hooks/useCount';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';

import { SEND_CODE_DELAY } from '../../constants/mainInformation.constants';
import styles from './PhoneModal.module.scss';

interface Actions {
  show: () => void;
  hide: () => void;
  toggle: (value?: boolean | undefined) => void;
}

interface Props {
  phoneNumber: string;
  selfStore: ISelfEmployeeStore;
  empStore: IEmployeeStore;
  modalVisibility: boolean;
  modalActions: Actions;
  showConfirmationModal: (phone: string) => void;
}

export const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

export const PhoneModal: FC<Props> = observer(({
  phoneNumber, selfStore, empStore, modalVisibility, modalActions, showConfirmationModal,
}) => {
  const { editPhone } = empStore;
  const { updatePhoneStatus } = selfStore;
  const formatPhone = formatPhoneNumber(phoneNumber);
  const { logger } = useAppStoreContext();

  const [form] = Form.useForm();

  const { count, setCount } = useCount();

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const onFinishConfirm = (values: any): void => {
    const mobilePhone = clearPhone(values.mobilePhone) ?? '';

    if (mobilePhone) {
      editPhone(mobilePhone).then(() => {
        modalActions.hide();
        showConfirmationModal(mobilePhone);
        setCount(SEND_CODE_DELAY);
      }).catch(error => {
        logger.toMessage('error', error.response?.data?.message || error.message);
      });

      if (mobilePhone) {
        updatePhoneStatus(!selfStore.isRequiredPhone);
      }
    }
  };

  return (
    <Modal
      title={null}
      footer={null}
      visible={modalVisibility}
      onCancel={() => {
        modalActions.hide();
        updatePhoneStatus(!selfStore.isRequiredPhone);
      }}
      destroyOnClose={true}
      className={styles.modal}
    >
      <div className={styles.cardWrapper}>
        <div className={styles.header}>
          <div className={styles.numberApplication}>
            Редактирование номера телефона
          </div>
          <div className={styles.cancel} onClick={modalActions.hide}>
            <img alt="Close" src={Close} />
          </div>
        </div>
        <span className={styles.phoneTitle}>
          Номер
        </span>
        <Form
          name="phone-form"
          form={form}
          onFinish={onFinishConfirm}
          preserve={false}
        >
          <FormField
            name="mobilePhone"
            placeholder="Телефон"
            type={FieldType.phone}
            initialValue={formatPhone}
            rules={[
              ValidationRules.general.required,
              ValidationRules.general.checkingEditingPhoneNumber(phoneNumber),
              {
                pattern: validationPatterns.numberValidation,
                message: 'Неправильный формат телефона',
              },
            ]}
          />
          <div className={styles.saveButton}>
            {count ? (
              <>
                <span>Отредактировать повторно через: </span>
                <span className={styles.count}>{`00:${count < 10 ? '0' : ''}${count}`}</span>
              </>
            ) : (
              <TButton
                htmlType="submit"
                $size="small"
              >
                Отправить код подтверждения
              </TButton>
            )}
          </div>
        </Form>
      </div>
    </Modal>
  );
});
