import React, { FC } from 'react';

import * as S from './ArrowControls.styles';

interface Props {
  handleListItemUp: () => Promise<void>;
  handleListItemDown: () => Promise<void>;
}

export const ArrowControls: FC<Props> = props => {
  const { handleListItemUp, handleListItemDown } = props;

  return (
    <S.Wrapper>
      <S.UpArrow onClick={handleListItemUp} />
      <S.DownArrow onClick={handleListItemDown} />
    </S.Wrapper>
  );
};
