import React, { FC } from 'react';
import { Form, Tooltip } from 'antd';

import { LabelDescriptionWrapper } from '../AdditionalServices.style';
import * as S from '../AdditionalServices.style';
import { ServiceItems } from '../types';

interface Props {
  isShadow?: boolean;
  item: ServiceItems;
  onChange: (value) => void;
  isLiftAvailable?: boolean | undefined;
  checked: boolean;
}

export const ServiceItem: FC<Props> = props => {
  return (
    <S.Item isShadow={props.item.name === 'lift' || props.item.name === 'car'}>
      <S.Label>
        <S.LabelTitle>{props.item.title}</S.LabelTitle>
        <LabelDescriptionWrapper>
          <S.LabelDescription>{props.item.description}</S.LabelDescription>
          {props.item?.icon && (
          <Tooltip title={props.item?.infoDescription}>{props.item?.icon}</Tooltip>
          )}
        </LabelDescriptionWrapper>
        <S.Price>{props.item.price}</S.Price>
      </S.Label>
      <S.ButtonContainer>
        {props.item.name === 'lift' && !props.isLiftAvailable ? (
          <S.LiftButtonContainer isLiftAvailable={props.isLiftAvailable}>
            <span>Доступно при заказе грузчиков</span>
          </S.LiftButtonContainer>
        ) : (
          <Form.Item name={props.item?.name} initialValue={props.item.value}>
            <S.Switch
              checked={props.checked}
              onChange={props.onChange}
            />
          </Form.Item>
        )}
      </S.ButtonContainer>
    </S.Item>
  );
};
