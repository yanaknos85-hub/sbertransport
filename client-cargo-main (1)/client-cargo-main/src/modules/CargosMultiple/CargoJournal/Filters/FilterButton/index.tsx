import React, { FC } from 'react';

import * as S from './filterButton.styles';

interface Props {
  handleFilters: () => void;
  title?: string;
  filledCount?: number;
}

export const FilterButton: FC<Props> = props => {
  const {
    handleFilters, title, filledCount,
  } = props;

  return (
    <S.Button
      data-count={filledCount}
      onClick={() => handleFilters()}
    >
      <S.Icon />
      <S.Text>{title}</S.Text>
    </S.Button>
  );
};
