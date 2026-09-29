import React, { FC } from 'react';
import { Modal } from 'antd';
import cn from 'classnames';

import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import Flex from 'components/Flex/Flex';
import { useTripsModal } from '../../context/TripsModal';

import styles from './comment.module.scss';

interface CommentProps {
  comments?: string[];
}

export const Comment: FC<CommentProps> = ({ comments }) => {
  const { openComment } = useTripsModal();

  if (!comments || !comments.length) {
    return <span>{EMPTY_CELL_CONTENT}</span>;
  }

  const handleOpenComment = () => {
    openComment(comments);
  };

  return (
    <Flex>
      <div
        className={styles.comment}
        title={comments.join('\n\n')}
        onClick={handleOpenComment}
      >
        {comments[0]}
      </div>

      {comments.length > 1 && (
        <div
          className={cn(styles.comment, styles.commentLength)}
          onClick={handleOpenComment}
        >
          {`+${comments.length - 1}`}
        </div>
      )}
    </Flex>
  );
};

export const CommentModal: FC = () => {
  const {
    isOpened, modalState, closeModal,
  } = useTripsModal();

  return (
    <Modal
      title="Комментарии"
      className={styles.modal}
      visible={isOpened('comment')}
      onCancel={closeModal}
      destroyOnClose
      footer={[]}
      width={500}
    >
      <Flex
        direction="column"
        gap={10}
        alignItems="stretch"
      >
        {modalState.comments?.map((comment, index) => (
          <div key={index}>
            {(modalState.comments?.length ?? 0) > 1 && <span>{`${index + 1}. `}</span>}
            {comment}
          </div>
        ))}
      </Flex>
    </Modal>
  );
};
