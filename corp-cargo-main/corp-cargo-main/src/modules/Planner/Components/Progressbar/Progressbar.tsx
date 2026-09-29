import React, { FC } from 'react';

import { colors } from 'shared/styles/styles';

import * as S from './Progressbar.styles';

interface Props {
  title: string;
  total: number | string;
  holdingCapacity: number;
  units: string;
  decimal?: boolean;
}

export const Progressbar: FC<Props> = props => {
  const {
    title, total, holdingCapacity, units, decimal,
  } = props;

  const percent = typeof total === 'number' ? (total / holdingCapacity) * 100 : 0;

  return (
    <>
      <S.ProgressInfo>
        <S.Title>{title}</S.Title>
        <S.Info>
          <p>
            <span>
              {total}
              /
              {holdingCapacity}
              {units}
              {decimal && <sup>3</sup>}
            </span>
          </p>
        </S.Info>
      </S.ProgressInfo>
      <S.ProgressBarStyled
        trailColor="#bfbfbf"
        strokeColor={{
          '0%': colors.green3,
          '100%': colors.green10,
        }}
        percent={percent}
        status="normal"
        showInfo={false}
        strokeLinecap="square"
      />
    </>
  );
};
