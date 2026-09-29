import React, { FC, useEffect } from 'react';

import * as S from './Cargos.style';

interface Props {
  title: string;
  width?: number;
  weight?: number;
  length?: number;
  height?: number;
  volume?: number;
  occupiedPlacesCount?: number;
}
interface ItemWrapperProps { title: string; children: React.ReactNode }

export const ItemOption: FC<Props> = props => {
  const [data, setData] = React.useState('');

  const {
    title, length, width, height, weight, occupiedPlacesCount, volume,
  } = props;

  useEffect(() => {
    if (length && width && height) {
      setData(`${length} * ${width} * ${height}`);
    }

    if (weight) {
      setData(`${weight}`);
    }

    if (volume) {
      setData(`${volume}`);
    }

    if (occupiedPlacesCount) {
      setData(`${occupiedPlacesCount}`);
    }
  }, [title, length, width, height, weight, volume, occupiedPlacesCount]);

  return (
    <S.CargoItemOption>
      <S.CargoItemOptionTitle>{title}</S.CargoItemOptionTitle>
      <S.CargoItemOptionValue>{data}</S.CargoItemOptionValue>
    </S.CargoItemOption>
  );
};

export const ItemOptionWrapper: FC<ItemWrapperProps> = ({ title, children }) => {
  return (
    <S.CargoItemOption>
      <S.CargoItemOptionTitleForNumber>{title}</S.CargoItemOptionTitleForNumber>
      <S.CargoItemOptionValue>{children}</S.CargoItemOptionValue>
    </S.CargoItemOption>
  );
};
