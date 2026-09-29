import styled from '@emotion/styled';

export const MainWrapper = styled.div`
  z-index: 1000;
`;

export const Container = styled(MainWrapper)<{ isRouteVisible: boolean }>`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 14px 24px;
  width: 100%;
  overflow: hidden;
  border-radius: ${({ isRouteVisible }) => isRouteVisible ? '12px 12px 0 0' : '12px'};
  background-color: white !important;
  border: none !important;
`;

export const TextSwitch = styled.span`
  margin-right: 12px;
  font-family: 'SB Sans Text Regular', serif;
  font-size: 16px;
  font-weight: 600;
`;

