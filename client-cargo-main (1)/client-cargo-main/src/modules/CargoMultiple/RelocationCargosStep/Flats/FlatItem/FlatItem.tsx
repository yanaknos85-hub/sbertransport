import React, { FC, ReactNode } from 'react';

import * as S from './FlatItem.styles';

interface Props {
  title: string;
  text: string;
  icon: ReactNode;
  isSelected: boolean;
  onClick: () => void;
}

export const FlatItem: FC<Props> = props => {
  const {
    title, text, icon, isSelected, onClick,
  } = props;

  return (
    <S.Wrapper onClick={onClick} isSelected={isSelected}>
      <S.Description>
        <S.Title>{title}</S.Title>
        <S.Text>{text}</S.Text>
      </S.Description>
      <S.Image>{icon}</S.Image>
    </S.Wrapper>
  );
};
