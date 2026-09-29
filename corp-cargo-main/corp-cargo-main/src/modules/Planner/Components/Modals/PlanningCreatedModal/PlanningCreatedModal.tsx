import React, { FC } from 'react';

import { useTranslation } from 'i18n';
import { ReactComponent as DoneIcon } from '../../../images/doneIcon.svg';
import { Container } from '../Container';

import * as S from './PlanningCreatedModal.styles';

interface Props {
  visible: boolean;
  handleOkModal: () => void;
  handleBackToListRoutes: () => void;
  onCancel: () => void;
}

export const PlanningCreatedModal: FC<Props> = props => {
  const {
    visible, handleOkModal, handleBackToListRoutes, onCancel,
  } = props;

  const { t } = useTranslation();

  return (
    <Container
      visible={visible}
      footer={null}
      centered
      width={400}
      onCancel={onCancel}
    >
      <S.ModalContainer>
        <DoneIcon />
        <S.Title>{t.Planner.changeRouteStatus}</S.Title>
        <S.ButtonsBlock>
          <S.ButtonOk onClick={() => handleOkModal()}>{t.Planner.ok}</S.ButtonOk>
          <S.Button onClick={() => handleBackToListRoutes()}>{t.Planner.toRoutesList}</S.Button>
        </S.ButtonsBlock>
      </S.ModalContainer>
    </Container>
  );
};
