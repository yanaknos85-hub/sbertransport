import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';
import { Button as ButtonAnt } from 'antd';

export const Container = styled.div<any>`
  display: ${props => (props.visible ? 'flex' : 'none')};
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 14px;
  position: absolute;
  top: 40%;
  right: 9%;
  width: 40%;
  background-color: ${colors.white};
  border-radius: 8px;
  z-index: 1000;
`;

export const Wrapper = styled.div`
  position: relative;
  width: 100%;
`;

export const Cross = styled.div`
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 100;
`;

export const ButtonOne = styled(ButtonAnt)`
  padding: 19px 20px;
  color: ${colors.green10};
  background-color: inherit;
  border: 2px solid ${colors.green10};
  border-radius: 8px;
  line-height: 0;

  &:hover {
    color: ${colors.white};
    background-color: ${colors.green10};
  }
`;

export const ButtonTwo = styled(ButtonAnt)`
  padding: 19px 20px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  line-height: 0;
  border: none;

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;

export const Content = styled.div`
  display: flex;
  justify-content: space-between;
`;

export const RouteParams = styled.div`
  p {
    margin: 0;
    padding: 0;
  }
`;

export const SemiBoldText = styled.p<any>`
  margin: 0;
  padding: 0;

  font-size: 16px;
  font-weight: 600;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};
`;
