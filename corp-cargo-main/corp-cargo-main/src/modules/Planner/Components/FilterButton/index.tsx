import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import * as S from './filterButton.styles';

interface Props {
  handleFilters: () => void;
}

export const FilterButton: FC<Props> = props => {
  const { handleFilters } = props;

  const { t } = useTranslation();

  return (
    <S.Button onClick={() => handleFilters()}>
      <S.Icon />
      <S.Text>{t.Planner.filters}</S.Text>
    </S.Button>
  );
};
