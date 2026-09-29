import { Button as ButtonAnt } from 'antd';
import { colors } from 'shared/styles/styles';
import styled from 'styled-components';

import { Container } from '../Modals/Container';

export const Modal = styled(Container)`
  .ant-modal-content .ant-modal-header .ant-modal-title {
    font-size: 20px;
    font-weight: 600;
  }
`;

export const Title = styled.h2`
  font-size: 20px;
  font-weight: 600;
`;

export const ButtonsBlock = styled.div`
  display: flex;
  justify-content: flex-end;
`;

export const Button = styled(ButtonAnt)<{ reset: boolean }>`
  padding: 20px 24px;
  margin-left: 12px;
  border-radius: 8px;
  color: ${props => (props.reset ? 'red' : colors.white)};
  background-color: ${props => (props.reset ? 'inherit' : colors.solidBodyNormal)};
  border: none;
  outline: none;
  line-height: 4px;

  &:hover {
    color: ${props => props.reset && colors.white};
    background-color: ${props => props.reset && colors.solidBodyNormal};
  }
`;
