import React, { FC } from 'react';
import { ReactComponent as UpdateDataIcon } from '../../images/updateData.svg';
import { Button } from '../Button/Button';

import * as S from './UpdateData.styles';

interface Props {
  handleUpdate?: () => void | undefined;
}

export const UpdateData: FC<Props> = props => {
  const { handleUpdate } = props;

  return (
    <S.Container>
      <Button onClick={() => handleUpdate?.()}>
          <UpdateDataIcon />
      </Button>
    </S.Container>
  );
};
