import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

import { Container } from '../Container';

export const Modal = styled(Container)`
  .ant-modal-content .ant-modal-header .ant-modal-title {
    color: ${colors.gray10};
    font-size: 20px;
    font-family: ${fontFamily.SBSansTextSemibold};
    font-weight: 600;
  }
`;

export const Title = styled.h2<any>`
  color: ${colors.gray10};
  font-size: 20px;
  font-family: ${fontFamily.SBSansTextSemibold};
  font-weight: 600;
`;

export const ButtonsBlock = styled.div`
  display: flex;
  justify-content: flex-end;
`;

export const ResetButton = styled(ButtonAnt)`
  padding: 20px 24px;
  margin-left: 12px;
  border-radius: 8px;
  color: red;
  background-color: inherit;
  border: none;
  outline: none;
  line-height: 4px;

  &:hover {
    color: white;
    background-color: #10BF6A;
  }
`;

export const ApplyButton = styled(ButtonAnt)`
  padding: 20px 24px;
  margin-left: 12px;
  border-radius: 8px;
  color: white;
  background-color: #10BF6A;
  border: none;
  outline: none;
  line-height: 4px;

  &:hover {
    color: #10BF6A;
    background-color: white;
  }
`;
