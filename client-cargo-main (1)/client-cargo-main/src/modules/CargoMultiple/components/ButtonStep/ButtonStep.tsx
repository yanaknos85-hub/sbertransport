import React, { FC } from 'react';

import * as S from './ButtonStep.styles';

interface Props {
  errors: string | boolean;
  handleNextStep: () => void;
}

export const ButtonStep: FC<Props> = props => {
  const { errors, handleNextStep } = props;

  return (
    <S.SideButtons>
      <S.SideButton disabled={!!errors} onClick={handleNextStep}>
        {errors || 'Далее'}
      </S.SideButton>
    </S.SideButtons>
  );
};
