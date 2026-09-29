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
    <S.Param>
      <S.ParamLabel>{title}</S.ParamLabel>
      <S.ParamValue>{data}</S.ParamValue>
    </S.Param>
  );
};
