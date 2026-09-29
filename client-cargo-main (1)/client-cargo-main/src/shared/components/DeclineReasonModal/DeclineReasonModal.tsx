import React, { FC, useState } from 'react';
import { ValidationRules } from 'shared/fieldValidationRules';
import { Modal } from 'shared/form/CargoType/CargoType.style';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import TButton from 'shared/ui/Button/Button';

import { IDeclineReason } from 'stores/Compensations/Compensation.interface';
import { ReactComponent as CloseIcon } from 'modules/Evaluation/static/icons/closeIcon.svg';

import TTypography from '../Cargo/TTypography';
import RowButtons from '../SchedulerMulti/RowButtons/RowButtons';

interface TDeclineReasonModal {
  requestIds: string | string[];
  visible: boolean;
  onOk(requestIds: string | string[], reason: IDeclineReason | string): void;
  onCancel(): void;
  isJournal?: boolean;
  isApproval?: boolean | undefined;
}

const DeclineReasonModal: FC<TDeclineReasonModal> = ({
  visible,
  onOk,
  onCancel,
  requestIds,
  isJournal,
  isApproval,
}) => {
  const [reason, setReason] = useState('');
  const [tagReason, setTagReason] = useState('');

  const DECLINE_REASONS_TAGS = ['Нет лимита', 'Ошибка', 'Изменение данных'];

  const okHandler = (): void => {
    if (reason || tagReason) {
      onOk(requestIds, `${tagReason}. ${reason}`);
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

  const onChangeReason = (values: number[]) => {
    const reasonString = values.map((value: number) => DECLINE_REASONS_TAGS[value]).join(', ');
    setTagReason(reasonString);
  };

  const handleChangeReason = (values: number[]) => {
    onChangeReason(values);
  };

  const title = (isJournal && !isApproval) || !isApproval ? 'Отменить?' : 'Отклонить?';
  const subStr = (isJournal && !isApproval) || !isApproval ? 'отмены' : 'отклонения';

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
      open={visible}
      onOk={okHandler}
      onCancel={cancelHandler}
      afterClose={clear}
      closeIcon={<CloseIcon />}
      okText="Отправить"
      okButtonProps={{ disabled: reason.length === 0, danger: true }}
      destroyOnClose={true}
      footer={[
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
        {`Пожалуйста, опишите причину ${subStr} заявки.`}
      </TTypography>
      <RowButtons
        label=""
        names={['Нет лимита', 'Ошибка', 'Изменение данных']}
        values={[0, 1, 2]}
        onChange={handleChangeReason}
      />
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
export default DeclineReasonModal;
