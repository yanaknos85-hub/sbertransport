import { Space } from 'antd';
import styled from 'styled-components';

export const SpinWrapper = styled.div`
  height: 100%;
  width: 100%;
  background: white;
`;

export const SideContent = styled('div')`
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  min-height: 100vh;

  @media (max-width: 767px) {
    height: auto;
  }
`;

export const SpinnerContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 9999;
`;

export const ButtonContainer = styled.div`
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 9998;
`;

export const SpaceStyled = styled(Space)`
  width: 100%;
  border-radius: 12px;
  background-color: #fff;
  padding: 16px 0;
`;
