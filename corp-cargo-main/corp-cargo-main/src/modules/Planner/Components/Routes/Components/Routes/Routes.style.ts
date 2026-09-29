import styled from 'styled-components';

export const MainWrapper = styled.div`
  width: 32%;
  z-index: 1000;
  min-width: 498px;
`;

export const Wrapper = styled(MainWrapper)`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  border-radius: 12px;
  position: absolute;
  left: 10px;
  top: 10px;
  bottom: 10px;
  z-index: 1000;
`;

export const Container = styled.div`
  width: 100%;
  height: 100%;
  overflow: auto;
  display: flex;
  flex-direction: column;
`

export const TabsStyled = styled.div`
    width: 500px;
    height: 100%;
    overflow: auto;
    display: flex;
    flex-direction: column;

    ::-webkit-scrollbar {
    display: none;
  }
`;

export const PaginationWrapper = styled.div`
  padding: 24px;
  background-color: white;
  border-radius: 0 0 12px 12px;
`;

export const Footer = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: auto;
  position: sticky;
  bottom: 0;
  left: 30px;
  background-color: white;
  z-index: 1000;
  box-shadow: 0 -22px 38px  30px white;
  gap: 16px;
  padding: 6px 24px;
`;
