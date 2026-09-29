import styled from 'styled-components';
import { MapComponent } from 'shared/components/Map/MapComponent';

export const PlannerStyled = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
`;

export const MainStyled = styled.div`
  display: flex;
  justify-content: space-between;
  flex-direction: row;
  position: relative;
  height: 100%;
`;

export const Map = styled(MapComponent)<{ isOrdersVisible: boolean }>`
  width: 140%;
  height: 100%;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  border-radius: 12px;
`;

export const Title = styled.h2`
  margin-left: 35%;
`;

export const UpdateBlock = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
  align-items: center;
`;

export const ButtonText = styled.span`
  margin-right: 8px;
`;
