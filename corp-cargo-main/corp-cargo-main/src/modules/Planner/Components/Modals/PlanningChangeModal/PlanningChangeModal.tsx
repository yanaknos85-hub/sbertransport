import React, { useState } from 'react';

import { ReactComponent as DoneIcon } from '../../../images/doneIcon.svg';
import { Container } from '../Container';
import * as S from './PlanningChangeModal.styles';

export const PlanningChangeModal = () => {
  const [isVisible, setIsVisible] = useState(false);

  const handleCancel = () => {
    setIsVisible(false);
  };

  return (
    <Container
      visible={isVisible}
      footer={null}
      centered
      width={400}
      onCancel={handleCancel}
    >
      <S.ModalContainer>
        <DoneIcon />
        <S.Title>Планирование изменено</S.Title>
        <S.ButtonsBlock>
          <S.ButtonOk>Просмотр</S.ButtonOk>
          <S.Button>В список маршрутов</S.Button>
        </S.ButtonsBlock>
      </S.ModalContainer>
    </Container>
  );
};
