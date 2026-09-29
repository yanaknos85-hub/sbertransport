import React, { FC, useState } from 'react';
import * as S from './Comment.styles';

interface commentProps {
  comment: string | null;
}

export const Comment: FC<commentProps> = ({ comment }) => {
  const [showFullComment, setShowFullComment] = useState(false);

  const toggleComment = () => {
    setShowFullComment(!showFullComment);
  };

  return (
    <S.CommentContainer>
      {comment && comment.length > 130 ? (
        showFullComment ? (
          <S.CommentText>
            {comment}
            {' '}
            <S.CommentLink className="comment-link" onClick={toggleComment}>
              Свернуть
            </S.CommentLink>
          </S.CommentText>
        ) : (
          <S.CommentText>
            {comment?.substring(0, 120)}
            ...
            {' '}
            <S.CommentLink className="comment-link" onClick={toggleComment}>
              Показать полностью
            </S.CommentLink>
          </S.CommentText>
        )
      ) : (
        <S.CommentText>{comment}</S.CommentText>
      )}
    </S.CommentContainer>
  );
};
