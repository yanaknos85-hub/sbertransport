import React, { FC } from 'react';
import { Moment } from 'moment';
import { IconButton } from 'shared/form/Button/Button';

import { DATE_FORMAT } from 'constants/constants.app';

import { ReactComponent as ArrowLeft } from './images/backward.svg';
import * as S from './StepHeader.style';

interface Props {
  title?: string;
  onChange?: () => void;
  desiredDate?: Moment;
  isRegular?: boolean;
  step?: number;
  children?: React.ReactNode;
}

const StepHeader: FC<Props> = props => {
  const {
    title,
    onChange,
    desiredDate,
    isRegular,
    children,
  } = props;
  return (
    <S.Header>
      <S.Inner>
        {onChange && (
          <S.Icon>
            <IconButton onClick={onChange} src={<ArrowLeft />} />
          </S.Icon>
        )}
        <S.Content>
          <S.Title>{title}</S.Title>
          {children}
        </S.Content>
      </S.Inner>
      {!!desiredDate && (
        <S.StepContainer>
          <S.StepTitle>Отправление: </S.StepTitle>
          {!isRegular && <S.StepDate>{desiredDate.format(DATE_FORMAT.DAY_MONTH_YEAR)}</S.StepDate>}
        </S.StepContainer>
      )}
    </S.Header>
  );
};

export default StepHeader;
