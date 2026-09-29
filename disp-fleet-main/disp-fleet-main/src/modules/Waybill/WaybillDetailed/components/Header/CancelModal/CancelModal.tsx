import React, { useState, type FC } from 'react';
import { Input, Modal } from 'antd';
import { useHistory } from '@sber-sbertransport/mf-core';
import cn from 'classnames';
import { useTranslation } from 'i18n';
import { useCancelEwb } from 'api/waybill/waybill.api';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import * as routes from 'constants/routes.constants';
import styles from './CancelModal.module.scss';

interface Props {
  visible: boolean;
  id: UUID;
  humanReadableId: string;
  onClose: () => void;
}

const CancelModal: FC<Props> = ({
  visible,
  id,
  humanReadableId,
  onClose,
}) => {
  const history = useHistory();

  const { Waybill: { modal: { cancel } } } = useTranslation().t;

  const [cancelEwb, { isLoading: isCanceling }] = useCancelEwb(id, humanReadableId);

  const [comment, setComment] = useState('');
  const isSubmitDisabled = comment.length < 10;

  const handleSubmit = () => {
    cancelEwb(comment)
      .then(() => history.replace(routes.RELEASE_ON_LINE_LINK))
      .catch(ignore);
  };

  return (
    <Modal
      visible={visible}
      width="450px"
      title={cancel.title}
      okText={cancel.okButtonText}
      okButtonProps={{ disabled: isSubmitDisabled, loading: isCanceling }}
      onCancel={onClose}
      onOk={handleSubmit}
    >
      <p className={styles.description}>{cancel.description}</p>

      <div className={styles.title}>{cancel.comment}</div>
      <Input.TextArea
        className={cn(styles.comment, {
          [styles.comment__good]: !isSubmitDisabled,
        })}
        allowClear
        maxLength={2048}
        autoSize={{ minRows: 5, maxRows: 5 }}
        placeholder={cancel.commentPlaceholder}
        value={comment}
        onChange={e => setComment(e.target.value)}
      />
    </Modal>
  );
};

export default CancelModal;
