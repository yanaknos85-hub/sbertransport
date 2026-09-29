import React, { FC } from 'react';

import Plus from '../../static/images/plus24.png';
import * as S from './AddressButton.styles';

interface Props {
  errors: string;
  handleAddAddress: (address: any) => void;
}

export const AddressButton: FC<Props> = props => {
  const { errors, handleAddAddress } = props;

  return (
    <S.SideButtons>
      <S.SideButton disabled={!!errors} onClick={handleAddAddress}>
        <span>
          <img src={Plus} alt="plus icon" />
          {errors || 'Добавить адрес'}
        </span>
      </S.SideButton>
    </S.SideButtons>
  );
};
