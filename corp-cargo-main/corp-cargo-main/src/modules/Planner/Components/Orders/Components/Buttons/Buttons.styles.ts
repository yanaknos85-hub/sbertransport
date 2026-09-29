import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { Link as LinkRouterDom } from 'react-router-dom';

import { colors } from 'shared/styles/styles';

export const ButtonsBlock = styled.div`
  display: flex;
  justify-content: flex-end;
  align-items: center;
  position: absolute;
  bottom: 70px;
  right: 12px;
  width: calc(100% - 12px);
  padding: 6px 24px;
  gap: 16px;
  background-color: ${colors.white};
  border-radius: 12px;
  z-index: 2;
`;
export const ButtonRouteStyled = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 600;
  padding: 19px 0;
  margin-left: 12px;
  line-height: 0;
  cursor: pointer;
`;

export const ButtonStyled = styled(ButtonAnt)`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 193px;
  font-weight: 600;
  padding: 19px 0;
  margin-left: 12px;
  line-height: 0;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;

export const Link = styled(LinkRouterDom)`
  padding: 15px 12px;
`;
