import { Button as ButtonAnt } from 'antd';
import { ReactComponent as SettingsIcon } from 'shared/images/cargo/settings.svg';
import { colors } from 'shared/styles/styles';
import styled, { css } from 'styled-components';

const CommonButtonStyle = css`
  display: flex;
  align-items: center;
  height: auto;
  border-radius: 8px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border: none;
  position: relative;
  padding: 8px 8px;

  &:focus,
  &:active {
    color: ${colors.white};
    background-color: ${colors.solidBodyNormal};
  }

  svg path {
    stroke: ${colors.white};
    transition: color 0.3s;
  }

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    border: 1px solid ${colors.solidBodyNormal};

    svg path {
      stroke: ${colors.solidBodyNormal};
      transition: all 0.3s;
    }
  }
`;

export const Button = styled(ButtonAnt)`
  ${CommonButtonStyle}

  &::after {
    content: attr(data-count); /* Отображаем значение из data-count */
    position: absolute;
    top: -6px;
    right: -6px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background-color: ${colors.white};
    color: ${colors.green10};
    font-size: 12px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid ${colors.green10};
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &[data-count]:not([data-count='0'])::after {
    opacity: 1; /* Показываем счётчик только если data-count > 0 */
  }
`;

export const Icon = styled(SettingsIcon)``;

export const Text = styled('p')`
  margin: 0;
`;
