import React, { FC } from 'react';
import { Button, Input } from 'antd';
import Modal from 'antd/lib/modal/Modal';

import { useReasonModal } from './useReasonModal';

export interface ReasonModalProps {
  onOkClick(): void;
  title?: string;
  buttonText?: string;
  isDanger?: boolean;
  placeholder?: string;
  instance?: ReturnType<typeof useReasonModal>;
}

export const ReasonModal: FC<ReasonModalProps> = ({
  onOkClick,
  title = 'Укажите причину отмены',
  buttonText = 'Отменить',
  isDanger = false,
  placeholder = 'Введите причину отмены',
  instance,
}) => {
  const {
    visible, actions, reasonText, setReasonText,
  // eslint-disable-next-line react-hooks/rules-of-hooks
  } = instance ?? useReasonModal();

  const onOkClickHandler = (): void => {
    onOkClick();
    actions.hide();
  };
  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>): void => setReasonText(e.target.value);

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={actions.hide}
      footer={(
        <Button
          danger={isDanger}
          onClick={onOkClickHandler}
          disabled={reasonText.length === 0}
        >
          {buttonText}
        </Button>
      )}
      closable={true}
      destroyOnClose={true}
    >
      <Input.TextArea
        placeholder={placeholder}
        value={reasonText}
        onChange={handleChange}
      />
    </Modal>
  );
};
