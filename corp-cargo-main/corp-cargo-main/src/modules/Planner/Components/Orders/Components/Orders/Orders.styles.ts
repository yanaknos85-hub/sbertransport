import styled, { css } from "styled-components";
import { Button as ButtonAnt, Tabs } from "antd";
import { colors } from "shared/styles/styles";
import { MainWrapper } from "../../../styles.common";

export const Container = styled(MainWrapper)<any>`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-top: ${(props) => (props.mt ? "65px" : 0)};
  padding: 0;
  background-color: ${colors.white};
  border-radius: 12px;
  position: absolute;
  top: 10px;
  right: 10px;

  ${({ full }) =>
    !!full &&
    css`
      bottom: 10px;
    `}
`;

export const ButtonStyled = styled(ButtonAnt)`
  padding: 19px 20px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  line-height: 0;
  border: none;
  margin-left: 12px;

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;

export const ButtonsBlock = styled.div`
  position: absolute;
  top: 16px;
  right: 16px;
`;

export const TabsStyled = styled(Tabs)`
  .ant-tabs-content-holder {
    height: 100%;
    overflow: auto;
  }
`;

export const Content = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  overflow: auto;
`;

export const PaginationWrapper = styled.div`
  padding: 24px;
`;

export const OrderListBlock = styled.div`
  display: flex;
  flex-direction: column;
  max-height: 100%;
  width: 100%;
`;
