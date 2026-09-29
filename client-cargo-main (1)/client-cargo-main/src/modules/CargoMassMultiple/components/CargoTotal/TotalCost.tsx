import React, { FC } from 'react';
import TTypography from 'shared/components/Cargo/TTypography';

import { DetailingTextLine } from './CargoTotal.style';

interface Props {
  allCost: number;
  title: string;
}

export const TotalCost: FC<Props> = props => {
  const { allCost, title } = props;

  const rub = <>&#8381;</>;

  return (
    <DetailingTextLine>
      <TTypography weight={600} size="16px">
        {title}
      </TTypography>
      <TTypography
        size="24px"
        weight={600}
        font="SB Sans Interface"
      >
        {allCost / 100}
        {' '}
        {rub}
      </TTypography>
    </DetailingTextLine>
  );
};
