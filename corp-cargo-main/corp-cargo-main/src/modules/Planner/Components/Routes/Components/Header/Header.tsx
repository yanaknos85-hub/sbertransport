import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { ReactComponent as Arrow } from '../../../../images/arrowLeft.svg';
import * as S from './Header.styles';

interface Props {
  addToRoute?: boolean;
  onClick: () => void;
}

export const Header: FC<Props> = props => {
  const { addToRoute, onClick } = props;

  return (
    <S.Header isAddToRoute={addToRoute}>
      {addToRoute && (
        <Tooltip title="Назад" placement="topLeft">
          <Arrow onClick={onClick} />
        </Tooltip>
      )}
    </S.Header>
  );
};
