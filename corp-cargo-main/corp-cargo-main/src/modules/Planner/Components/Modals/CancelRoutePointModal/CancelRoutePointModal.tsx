import React, { useState } from 'react';

import { Button as ButtonAnt } from 'antd';
import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

import * as S from './CancelRoutePointModal.styles';

import { Container } from '../Container';

export const ButtonOk = styled(ButtonAnt)<any>`
  padding: 20px 24px;
  line-height: 0;
  border-radius: 8px;
  border: none;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  outline: none;

  &:hover {
    color: ${colors.gray10};
    background-color: ${colors.white};
  }
`;

export const CancelRoutePointModal = () => {
  const [isVisible, setIsVisible] = useState(true);

  const handleCancel = () => {
    setIsVisible(false);
  };

  return (
    <Container
      visible={isVisible}
      footer={null}
      centered
      width={500}
      onCancel={handleCancel}
    >
      <S.ModalContainer>
        <S.Title>Удалить заявку?</S.Title>
        <S.Text>При удалении точки сбора Звенигордская 32, вы также удалите точку доставки Тверская 4</S.Text>
        <S.ButtonsBlock>
          <S.ButtonCancel cancel onClick={handleCancel}>
            Отменить
          </S.ButtonCancel>
          <ButtonOk>Создать</ButtonOk>
        </S.ButtonsBlock>
      </S.ModalContainer>
    </Container>
  );
};
