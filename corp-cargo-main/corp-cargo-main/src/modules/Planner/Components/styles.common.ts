import styled, { css } from 'styled-components';
import { Button as ButtonAnt, Tabs as TabsAnt, Divider as DividerAnt } from 'antd';
import { TabPane } from 'shared/components/Tab/TabPane';
import { colors, fontFamily } from 'shared/styles/styles';
import { MapComponent } from 'shared/components/Map/MapComponent';

export const PlannerStyled = styled.div`
  width: 100%;
  height: calc(100vh - 162px);
  overflow: hidden;
  position: relative;
`;

export const MainStyled = styled.div`
  display: flex;
  justify-content: space-between;
  flex-direction: row;
  flex: 1;
  position: relative;
  height: 100%;
  border: 2px solid ${colors.gray3};
  border-radius: 12px;
  transition: border-color 0.2s ease;
`;

export const CardWrapper = styled.div`
  width: 40%;
  z-index: 1000;
`;

export const MainWrapper = styled.div`
  width: 32%;
  z-index: 1000;
  min-width: 498px;
`;

export const ContainerStyled = styled(MainWrapper)`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  background-color: ${colors.white};
  border-radius: 12px;
  min-width: 100%;
  padding: 24px 24px 0 24px;
`;

export const MapContainer = styled.div`
  padding: 20px 24px;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
`;

export const Map = styled(MapComponent)`
  width: 140%;
  height: 100%;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  border-radius: 12px;
`;

export const Header = styled.div`
  width: 100%;
`;

export const TitleBlock = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
`;

export const TextBlock = styled.div`
  display: flex;
  flex-direction: column;
`;

export const HeaderTitle = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  cursor: pointer;
  span {
    margin-left: 16px;
  }
`;

export const Title = styled.h3`
  margin: 12px 0;
  font-size: 18px;
  font-weight: 600;
`;

export const List = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

export const Divider = styled(DividerAnt)`
  margin: 12px 0;
`;

export const TextLight = styled.p`
  margin-bottom: 0;
  font-family: ${fontFamily.SBSansTextRegular};
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: ${colors.gray8};
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

export const Price = styled.div`
  display: flex;
  justify-content: space-between;

  p {
    margin: 0;
    padding: 0;
  }

  p:nth-child(1) {
    font-size: 16px;
    font-family: ${fontFamily.SBSansTextRegular};
    font-weight: 400;
  }

  p:nth-child(2) {
    font-size: 22px;
    font-family: ${fontFamily.SBSansInterface};
    font-weight: 700;
    line-height: 32px;
    color: ${colors.black90Alpha};
  }
`;

export const Tabs = styled(TabsAnt)`
  position: relative;
  width: 100%;
  height: 100%;
`;

export const Wrapper = styled.div`
  overflow: auto;
`;

export const Container = styled(MainWrapper)<{ isOrderVisible: boolean }>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  max-height: 110px;
  min-height: 20px;
  overflow: hidden;
  border-radius: ${({ isOrderVisible }) => isOrderVisible? '12px 12px 0 0' : '12px'};
  background-color: white !important;
  border: none !important;

  ${({ mt }) => mt
  && css`
      top: 0;
      right: 0;
    `}
`;

export const SpinWrapper = styled.div`
  width: 100%;
  height: 100%;
  background-color: white;
  border-radius: 12px;
`;
