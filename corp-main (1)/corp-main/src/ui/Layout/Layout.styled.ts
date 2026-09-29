import styled from 'styled-components';

export const Header = styled.div`
  height: 64px;
  background-color: white;
  flex: 0 0 auto;
  box-shadow: 0px 0px 1px rgba(0, 0, 0, 0.12), 0px 3px 10px -6px rgba(0, 0, 0, 0.12);
  z-index: 1;
  padding: 0 24px;
  align-items: center;
  display: flex;
  line-height: normal;
`;

export const Drawer = styled.nav`
  width: 232px;
  min-width: 232px;
  flex: 0 0 auto;
  padding: 16px;
  padding-left: 24px;
  overflow: auto;
`;

export const Layout = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 5%);
`;

export const LayoutBody = styled.main`
  width: 100%;
  max-width: 100%;
  display: flex;
  flex-direction: row;
  background-color: #f2f3f6;
  flex: 1 1 auto;
  overflow: hidden;
`;

export const Body = styled.main`
  padding: 16px;
  padding-top: 0;
  flex: 1 1 auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

export const Content = styled.div`
  flex: 1 1 auto;
  overflow: auto;
`;

export const Spacer = styled.div`
  height: 16px;
`;
