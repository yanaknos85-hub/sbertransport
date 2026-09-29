import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors } from 'shared/styles/styles';

export const Footer = styled.div`
  display: flex;
  align-items: start;
  justify-content: space-between;
  margin-bottom: -10px;
`;

export const ButtonStyled = styled(ButtonAnt)`
  padding: 19px 20px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  line-height: 0;
  border: 1px solid ${colors.solidBodyNormal};

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    border: 1px solid ${colors.solidBodyNormal};
  }
`;
