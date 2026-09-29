import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 12px;
  width: 100%;
`;

export const ListItem = styled.div<any>`
  display: flex;
  flex-direction: column;
  padding: 14px 20px;
  margin-bottom: 20px;
  position: relative;
  border: 1px solid ${props => (props.active ? `${colors.green10}` : `${colors.gray3}`)};
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(255, 255, 255, 0.5);
  background: #ffffff;
  flex: 1 0 0;
  cursor: pointer;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const HeaderText = styled.div`
  display: flex;
  flex-direction: column;

  p {
    font-family: ${fontFamily.SBSansTextRegular};
    margin: 0;
    padding: 0;
    font-size: 14px;
  }

  p:nth-child(-n + 2) {
    color: ${colors.gray6};
  }

  p:nth-child(3) {
    color: ${colors.black};
    font-size: 18px;
    font-weight: 600;
    font-family: ${fontFamily.SBSansInterface};
  }
`;

export const Checkbox = styled.div`
  display: flex;
  align-items: center;

  .ant-checkbox-wrapper {
    margin-left: 10px;
  }

  .ant-checkbox-inner {
    width: 24px;
    height: 24px;
    border: ${`1px solid ${colors.gray4}`};
    border-radius: 4px;
  }
`;

export const Price = styled.div`
  display: flex;
  justify-content: space-between;

  p {
    margin: 0;
    padding: 0;
  }

  p:nth-child(1) {
    font-size: 14px;
    font-family: ${fontFamily.SBSansTextRegular};
    font-weight: 400;
    color: ${colors.gray8};
  }

  p:nth-child(2) {
    font-size: 24px;
    font-family: ${fontFamily.SBSansInterface};
    font-weight: 700;
    line-height: 32px;
    color: ${colors.black90Alpha};
  }
`;

export const Cost = styled.div`
  display: flex;
  flex-direction: column;
`;

export const CostTitle = styled.p`
  font-size: 14px;
  font-family: ${fontFamily.SBSansTextRegular};
  color: ${colors.gray7};
`;

export const ButtonStyled = styled(ButtonAnt)`
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

export const Footer = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
`;

export const ListItemBlock = styled.div`
  display: flex;
`;

export const HeaderBlock = styled.div`
  flex: 1;
`;

export const OrderInfoBlock = styled.div`
  flex: 1;
  padding-top: 10px;
`;
