import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';
import { Button as ButtonAnt } from 'antd';

export const ModalContainer = styled.div`
  padding: 8px;
`;

export const Title = styled.div`
  font-size: 20px;
  font-weight: 600;
  font-family: ${fontFamily.SBSansInterface};
  margin-bottom: 8px;
`;

export const Text = styled.p`
  margin-bottom: 32px;
  font-family: ${fontFamily.SBSansTextRegular};
  color: ${colors.gray7};
`;

export const ButtonsBlock = styled.div`
  display: flex;
  justify-content: flex-end;
`;

export const ButtonCancel = styled(ButtonAnt)<any>`
  padding: 20px 24px;
  margin-right: 12px;
  line-height: 0;
  border-radius: 8px;
  color: ${colors.gray9};
  background-color: ${colors.normalOff};
  border: none;

  :hover {
    background-color: ${colors.gray9};
    color: ${colors.white};
  }
`;
