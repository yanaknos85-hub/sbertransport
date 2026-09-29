import React, { FC, ReactNode } from 'react';
import { observer } from 'mobx-react';
import { CustomInputNumber } from 'shared/components/CustomInputNumber/CustomInputNumber';

import * as S from './FurnitureItem.style';

interface Props {
  title: string;
  subtitle?: string;
  image?: ReactNode;
  occupiedPlacesCount: number;
  onChange: (value: number | undefined) => void;
}

export const FurnitureItem: FC<Props> = observer(({
  title,
  subtitle,
  image,
  occupiedPlacesCount,
  onChange,
}) => {
  return (
    <S.Wrapper>
      <S.Description>
        <div>
          {image}
        </div>
        <S.TitleWrapper>
          <S.Title>{title}</S.Title>
          {subtitle && <S.Subtitle>{subtitle}</S.Subtitle>}
        </S.TitleWrapper>
      </S.Description>
      <CustomInputNumber
        width="130px"
        height="40px"
        min={0}
        defaultValue={1}
        max={100}
        value={occupiedPlacesCount}
        onChange={onChange}
      />
    </S.Wrapper>
  );
});
