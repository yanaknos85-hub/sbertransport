import { Button as ButtonAnt } from 'antd';

import { colors } from 'shared/styles/styles';
import styled, { css } from 'styled-components';

const CommonButtonStyle = css`
  margin-left: 12px;
  height: auto;
  border-radius: 8px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border: none;

  @media screen and (max-width: 1600px) {
    margin-top: 15px;
    margin-left: 0;
  }
`;

export const Button = styled(ButtonAnt)`
  ${CommonButtonStyle};
  padding: 9px 20px;
  border: 1px solid ${colors.solidBodyNormal};

  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};

  :active:hover {
    color: ${colors.white};
    background-color: ${colors.solidBodyNormal};
  }

  :focus {
    color: ${colors.white};
    background-color: ${colors.solidBodyNormal};
  }

  :focus:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    border: 1px solid ${colors.solidBodyNormal};
  }
`;
