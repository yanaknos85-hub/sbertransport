import React, { FC } from 'react';
import { Button as AntButton } from 'antd';
import { NativeButtonProps } from 'antd/lib/button/button';
import styled, { css } from 'styled-components';

interface TButton {
  $size?: 'small' | 'middle' | 'large';
  $makeLikeLink?: boolean;
  $fontWeight?: 'normal' | 'bold';
  $statusEvaluation?: boolean;
  href?: string;
  $isMobile?: boolean;
}

const StyledButton = styled(AntButton)<TButton>`
  border-color: var(--jade);
  color: var(--solitude);
  line-height: 24px;
  border-radius: calc(3 * var(--indent-fourth));

  font-weight: ${props => (props.$fontWeight === 'normal' ? 400 : 600)};
  font-size: ${props => (props.$size === 'small' ? '14px' : '16px')};
  padding: ${props => (props.$isMobile ? '0' : (props.$size === 'small' ? '9px 20px' : '4px 0'))};
  height: ${props => (props.$size === 'small' ? '40px' : '52px')};
  width: ${props => (props.$size === 'small' ? 'inherit' : '100%')};

  background-color: ${props => (props.$makeLikeLink ? 'var(--solitude)' : 'var(--jade)')};
  color: ${props => (props.$makeLikeLink ? 'var(--matterhorn)' : 'var(--solitude)')};
  height: ${props => props.$makeLikeLink && '60px'};
  border: ${props => props.$makeLikeLink && 'none'};
  display: ${props => props.$makeLikeLink && 'flex'};
  justify-content: ${props => props.$makeLikeLink && 'center'};
  align-items: ${props => props.$makeLikeLink && 'center'};
  width: ${props => props.$makeLikeLink && '100%'};

  &:hover {
    color: ${props => props.$makeLikeLink && 'var(--matterhorn)'};
  }

  ${({ $statusEvaluation }) => $statusEvaluation
  && css`
      background-color: #f2f3f6 !important;
      color: #4d4d4d !important;
      border-color: #f2f3f6 !important;
      margin-right: 10px;
    `}
`;

/**
 * TButton - кастомный компонент кнопки проекта Сбертранспорт
 *
 * @param $size в соответствие с гайдлайном возможны три вида кнопки - small, middle, large
 * @param $makeLikeLink убирает все стили связанные с кнопкой (ховеры, бордеры, цвет заливки) и превращает в обычный текст
 * */

const TButton: FC<TButton & NativeButtonProps> = props => <StyledButton {...props} />;

export default TButton;
