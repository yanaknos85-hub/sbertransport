import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';
import { Button as ButtonAnt } from 'antd';

export const ModalContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

export const Title = styled.div`
  font-size: 20px;
  font-weight: 600;
  font-family: ${fontFamily.SBSansInterface};
  margin-top: 24px;
  margin-bottom: 20px;
  text-align: center;
`;

export const Button = styled(ButtonAnt)`
  padding: 19px 20px;
  margin-left: 12px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  line-height: 0;
  border: none;
  outline: none;

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;

export const ButtonOk = styled(ButtonAnt)`
  padding: 18px 16px;
  width: fit-content;
  margin-right: 12px;
  line-height: 0;
  color: ${colors.gray9};
  border-radius: 8px;
  background: ${colors.normalOff};

  :hover {
    background-color: ${colors.gray9};
    color: ${colors.white};
  }
`;

export const ButtonsBlock = styled.div`
  display: flex;
`;
