import React, { FC } from 'react';
import Checkbox, { CheckboxChangeEvent } from 'antd/lib/checkbox/Checkbox';
import { DefaultValues } from 'constants/constants.app';
import { CargoInfo } from '../CargoInfo/CargoInfo';

import * as S from './OrderInfo.styles';

interface Props {
  weightR: number | DefaultValues;
  volumeR: number | DefaultValues;
  occupiedPlacesCount: number | null;
  value: string;
  checked: boolean;
  onClick: (e: any) => void;
  onChange: (e: CheckboxChangeEvent) => void;
}

export const OrderInfo: FC<Props> = props => {
  const {
    weightR, volumeR, occupiedPlacesCount, onClick, value, checked, onChange,
  } = props;

  return (
    <S.OrderInfo>
      <CargoInfo weightR={weightR} volumeR={volumeR} occupiedPlacesCount={occupiedPlacesCount} />
      <S.Checkbox onClick={onClick}>
        <Checkbox
          checked={checked}
          value={value}
          onChange={onChange}
        />
      </S.Checkbox>
    </S.OrderInfo>
  );
};
