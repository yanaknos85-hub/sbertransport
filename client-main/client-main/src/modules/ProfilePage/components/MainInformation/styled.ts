import styled from 'styled-components';
import { Button as UIButton } from '@sber-sbertransport/ui-kit/src';

const MainInformationWrapper = styled.div`
  display: flex;
  flex-wrap: wrap;
  padding: 0 16px;
  gap: 8px 0;

  @media (min-width: 501px) {
    padding: 12px 24px;
    gap: 16px 0;
  }
`;

const BlockInformation = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 250px;

  @media (min-width: 501px) {
    width: 33%;
  }
`;

const TitleInformation = styled.span`
  color: rgb(144, 144, 144);
  font-family: SB Sans Interface;
  font-size: 12px;
  font-weight: 400;
  line-height: 16px;
  letter-spacing: 0.2px;
`;

const DescriptionInformation = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

const NumberWrapper = styled.div`
  display: flex;
  align-items: center;

  svg {
    margin-left: 11px;
    cursor: pointer;
  }
`;

const Button = styled(UIButton)`
  height: 22px;
  padding: 0 16px;
  border-radius: 4px;
  font-weight: 500;
  letter-spacing: -0.3px;
`;

const ButtonConfirm = styled(Button)`
 margin-left: 8px;
`;

export {
  MainInformationWrapper, BlockInformation, TitleInformation, DescriptionInformation, NumberWrapper, Button, ButtonConfirm
};
