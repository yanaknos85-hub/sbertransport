import React, { FC } from 'react';
import { Comment } from '../Comment/Comment';

import * as S from './Footer.styles';

interface Props {
  comment: string | null;
}

export const Footer: FC<Props> = props => {
  const { comment } = props;
  return (
    <S.Footer>
        <Comment comment={comment} />
    </S.Footer>
  );
};
