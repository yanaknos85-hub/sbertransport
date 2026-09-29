import { Button as ButtonAnt } from 'antd';

import { colors } from 'shared/styles/styles';
import styled, { css } from 'styled-components';
import { ReactComponent as SettingsIcon } from '../../images/settings.svg';

const CommonButtonStyle = css`
  display: flex;
  align-items: center;
  margin-left: 12px;
  height: auto;
  border-radius: 8px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border: none;
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
  ${CommonButtonStyle};
  padding: 9px 20px;
`;

export const Icon = styled(SettingsIcon)`
  margin-right: 10px;
`;

export const Text = styled('p')`
  margin: 0;
`;
