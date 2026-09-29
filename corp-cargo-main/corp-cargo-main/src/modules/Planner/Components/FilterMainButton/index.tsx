import React, { FC } from 'react';
import * as S from './filterMainButton.styles';

interface Props {
  handleFilters: () => void;
}

export const FilterMainButton: FC<Props> = props => {
  const { handleFilters } = props;

  const handleClick = () => {
    handleFilters();
  };

  return (
    <S.Button onClick={handleClick}>
      <S.Icon />
    </S.Button>
  );
};
