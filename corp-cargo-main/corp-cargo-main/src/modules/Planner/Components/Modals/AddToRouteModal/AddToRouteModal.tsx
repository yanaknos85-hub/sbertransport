import React, { FC } from 'react';

import FormField from 'shared/form/FormField/FormField';
import { FieldType } from 'shared/form/Field/Field';
import { ValidationRules } from 'shared/fieldValidationRules';

import { Link } from 'react-router-dom';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { Container } from '../Container';
import * as S from './AddToRouteModal.styles';

interface Props {
  title?: string;
  text?: string;
  visible: boolean;
  handleCancel: () => void;
  isNew?: boolean;
}

export const AddToRouteModal: FC<Props> = props => {
  const {
    title, text, visible, handleCancel, isNew,
  } = props;

  const { handleAddToRoute } = usePlanner();

  const field = {
    route: {
      label: 'Маршрут',
      name: 'route',
      type: FieldType.select,
      index: 0,
      params: {
        options: [
          {
            label: 'RP 1234682522',
            value: 'RP 1234682522',
          },
          {
            label: 'RP 1234682522',
            value: 'RP 1234682522',
          },
          {
            label: 'RP 1234682522',
            value: 'RP 1234682522',
          },
          {
            label: 'RP 1234682522',
            value: 'RP 1234682522',
          },
        ],
      },
      rules: [ValidationRules.general.required],
    },
  };

  return (
    <Container
      visible={visible}
      footer={null}
      centered
      width={440}
      onCancel={handleCancel}
    >
      <S.ModalContainer>
        <S.Title>{title}</S.Title>
        <S.Text>{text}</S.Text>
        {!isNew && <FormField {...field.route} />}
        <S.ButtonsBlock>
          <S.ButtonOkStyled onClick={() => handleCancel()}>Отменить</S.ButtonOkStyled>
          <Link to="/planner/plannerRoutes">
            <S.ButtonStyled onClick={handleAddToRoute}>Добавить</S.ButtonStyled>
          </Link>
        </S.ButtonsBlock>
      </S.ModalContainer>
    </Container>
  );
};
