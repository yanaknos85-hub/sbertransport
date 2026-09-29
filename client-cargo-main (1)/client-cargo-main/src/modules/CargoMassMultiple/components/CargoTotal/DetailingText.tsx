import React, { FC } from 'react';
import TTypography from 'shared/components/Cargo/TTypography';

import { DetailingTextLine } from './CargoTotal.style';

interface Props {
  courierTariff: {
    cost: number;
    count: number;
  };
  title: string;
  mb: string;
}

export const DetailingText: FC<Props> = props => {
  const {
    courierTariff, title, mb,
  } = props;

  const rub = <>&#8381;</>;

  return (
    <DetailingTextLine mb={mb}>
      <TTypography>
        {title}
        {courierTariff.count > 1 && ` x${courierTariff.count}`}
      </TTypography>
      <TTypography>
        {courierTariff.cost / 100}
        {' '}
        {rub}
      </TTypography>
    </DetailingTextLine>
  );
};
