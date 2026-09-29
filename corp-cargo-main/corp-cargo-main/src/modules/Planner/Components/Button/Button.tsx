import React, { FC } from 'react';

import * as S from './Button.styles';

interface ButtonProps {
  onClick: () => void;
  children: JSX.Element | string;
}

export const Button: FC<ButtonProps> = props => {
  const { onClick, children } = props;

  return <S.Button onClick={onClick}>{children}</S.Button>;
};
