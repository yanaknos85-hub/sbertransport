import React, { FC } from 'react';

import TTypography from '../TTypography';
import { DetailingTextLine } from './CargoTotal.styleMulitple';

interface Props {
  title?: string;
  total?: string | number | null;
  mb?: string;
  ml?: string;
  titleSize?: string;
  titleColor?: string;
  titleFontWeight?: number;
  titleFontFamily?: string;
  textSize?: string;
  textColor?: string;
  textFontWeight?: number;
  textFontFamily?: string;
}

export const DetailingTextMulitple: FC<Props> = ({
  title,
  total,
  mb,
  ml,
  titleFontWeight,
  textFontWeight,
  titleSize,
  textSize,
  titleFontFamily,
  textFontFamily,
  titleColor,
  textColor,
}) => {
  return (
    <DetailingTextLine mb={mb} ml={ml}>
      <TTypography
        weight={titleFontWeight}
        size={titleSize}
        font={titleFontFamily}
        color={titleColor}
      >
        {title}
      </TTypography>
      <TTypography
        weight={textFontWeight}
        size={textSize}
        dangerouslySetInnerHTML={{ __html: total }}
        font={textFontFamily}
        color={textColor}
      />
    </DetailingTextLine>
  );
};
