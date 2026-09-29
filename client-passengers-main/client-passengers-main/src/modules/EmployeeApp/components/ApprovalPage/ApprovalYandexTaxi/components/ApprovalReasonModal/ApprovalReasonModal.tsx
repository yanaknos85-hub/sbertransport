import React, { FC, useState } from 'react';

import TTypography from 'shared/components/Cargo/TTypography';
import { ValidationRules } from 'shared/fieldValidationRules';
import { Modal } from 'shared/form/CargoType/CargoType.style';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import TButton from 'shared/ui/Button/Button';
import { IDeclineReason } from 'stores/Trip/Trip.interface';

interface TApprovementReasonModal {
  id: string;
  visible: boolean;
  onOk(id: string, reason: IDeclineReason | string): void;
  onCancel(): void;
  isJournal?: boolean;
  isApproval?: boolean;
}

const ApprovalReasonModal: FC<TApprovementReasonModal> = ({
  visible, onOk, onCancel, id,
}) => {
  const [reason, setReason] = useState('');
  const [tagReason, setTagReason] = useState('');

  const okHandler = (): void => {
    if (reason || tagReason) {
      onOk(id, { reason: `${tagReason}. ${reason}`, code: 0 });
    }
  };

  const cancelHandler = (): void => {
    setTagReason('');
    onCancel();
  };

  const clear = (): void => {
    setReason('');
    setTagReason('');
  };

  const title = 'Отклонить?';

  const field = {
    declineForm: {
      value: reason,
      name: 'reason',
      type: FieldType.textarea,
      rules: [ValidationRules.general.maxLength(180)],
      index: 0,
      params: {
        placeholder: 'Пожалуйста, укажите причину отклонения',
        maxLength: 180,
        autoSize: { minRows: 3, maxRows: 5 },
        onChange(e: React.ChangeEvent<HTMLInputElement>) {
          setReason(e.target.value);
        },
      },
    },
  };

  return (
    <Modal
      title={title}
      visible={visible}
      onOk={okHandler}
      onCancel={cancelHandler}
      afterClose={clear}
      okText="Отправить"
      okButtonProps={{ disabled: reason.length === 0, danger: true }}
      destroyOnClose={true}
      footer={[
        <TButton
          $size="small"
          key="cancel"
          onClick={cancelHandler}
          style={{
            cursor: 'pointer',
            backgroundColor: '#F2F3F6',
            color: '#4D4D4D',
            borderColor: '#F2F3F6',
          }}
        >
          Отмена
        </TButton>,
        <TButton
          $size="small"
          key="submit"
          onClick={okHandler}
          style={{
            cursor: reason || tagReason ? 'pointer' : 'not-allowed',
            backgroundColor: '#F2F3F6',
            color: '#4D4D4D',
            borderColor: '#F2F3F6',
          }}
        >
          Отклонить
        </TButton>,
      ]}
    >
      <TTypography
        color="#909090"
        size="16px"
        weight="400"
        font="SB Sans Text"
        mb="10px"
        margin="024px"
      >
        Пожалуйста, опишите причину отклонения заявки.
      </TTypography>

      <TTypography
        color="#262626"
        size="14px"
        weight="400"
        font="SB Sans Text"
        mb="10px"
      >
        Комментарий
      </TTypography>
      <FormField {...field.declineForm} />
    </Modal>
  );
};

export default ApprovalReasonModal;
