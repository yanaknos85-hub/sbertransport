import React, { FC } from 'react';
import { observer } from 'mobx-react';

import * as S from './RelocationAuthor.styles';

interface Sender {
  fullName?: string;
  mobilePhone?: string;
}

interface Props {
  sender: Sender | null;
  internalNote?: string;
}

const RelocationAuthor: FC<Props> = observer(({ sender, internalNote }) => {
  return (
    <S.AuthorContainer>
      <S.AuthorRow>
        <S.AuthorInfo>
          <S.AuthorValue>{sender?.fullName}</S.AuthorValue>
          <S.AuthorPhoneValue>{sender?.mobilePhone}</S.AuthorPhoneValue>
        </S.AuthorInfo>

        <S.NoteInfo>
          <S.NoteLabel>Номер служебной записки</S.NoteLabel>
          <S.NoteValue>{internalNote}</S.NoteValue>
        </S.NoteInfo>
      </S.AuthorRow>
    </S.AuthorContainer>
  );
});

export default RelocationAuthor;
