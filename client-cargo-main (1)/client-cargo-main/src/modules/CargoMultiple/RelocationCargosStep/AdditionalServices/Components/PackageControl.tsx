import React, { FC } from 'react';

import { LabelDescriptionWrapper } from '../AdditionalServices.style';
import * as S from '../AdditionalServices.style';
import { Package } from '../types';

interface Props {
  pkg: Package;
  onChange: (value) => void;
  onClick: () => void;
}

export const PackageControl: FC<Props> = props => {
  const {
    pkg, onClick, onChange,
  } = props;

  return (
    <S.Item>
      <S.Label>
        <S.LabelTitle>{pkg.name}</S.LabelTitle>
        <LabelDescriptionWrapper>
          <S.LabelDescription>
            Единица измерения:
            {pkg.unit}
          </S.LabelDescription>
        </LabelDescriptionWrapper>
      </S.Label>
      <S.ButtonContainer>
        {pkg.count === undefined || pkg.count <= 0 ? (
          <S.StyledButton
            onClick={onClick}
          >
            Добавить
          </S.StyledButton>
        ) : (
          <S.StyledInputNumber
            min={0}
            max={999}
            value={props.pkg.count}
            onChange={onChange}
          />
        )}
      </S.ButtonContainer>
    </S.Item>
  );
};
