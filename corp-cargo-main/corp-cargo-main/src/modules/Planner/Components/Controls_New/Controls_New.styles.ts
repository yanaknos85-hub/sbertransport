import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const ControlsStyled = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px 24px 0 24px;
  z-index: 1000;
  background-color: white;

  & .ant-input {
    background-color: ${colors.bgDark};
  }
`;

export const Wrapper = styled('div')`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const WrapperFilter = styled.div`
  width: 88%;
`;

export const ButtonsBlock = styled.div`
  display: flex;
  justify-content: flex-start;
  width: 50%;

  @media screen and (max-width: 1600px) {
    margin-top: 16px;
  }
`;

export const CheckRoutes = styled.div`
  color: rgb(16, 191, 106);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
`;
