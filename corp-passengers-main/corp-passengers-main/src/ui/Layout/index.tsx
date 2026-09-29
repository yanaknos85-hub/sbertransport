/* eslint-disable @stylistic/jsx-one-expression-per-line */
import * as React from 'react';
import { useLocation } from 'react-router-dom';
import { APP_NAME, NETWORK_LOOP } from 'constants/constants.env';
import styled from 'styled-components';
import { MaybeBreadcrumbs } from 'shared/components/Breadcrumbs';
import Header from '../Header/Header';
import SideMenu from '../SideMenu/SideMenu';

const Layout = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 5%);
`;

const Body = styled.div`
  width: 100%;
  max-width: 100%;
  display: flex;
  flex-direction: row;
  background-color: #f7f8fa;
  flex: 1 1 auto;
  overflow: hidden;
`;

const BodyMenu = styled.nav`
  flex: 0 0 auto;
  padding: 16px;
  overflow: auto;
`;

const BodyContent = styled.main`
  padding: 16px;
  padding-top: 0;
  flex: 1 1 auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

const Content = styled.div`
  flex: 1 1 auto;
  overflow: auto;
`;

const LayoutFC: React.FC<{
  headerContent?: React.ReactNode;
  sideMenuContent?: React.ReactNode;
}> = ({
  headerContent,
  sideMenuContent,
  children,
}) => {
  const contentRef = React.useRef<HTMLDivElement>(document.createElement('div'));
  const { pathname } = useLocation();

  React.useEffect(() => {
    if (contentRef.current !== null) {
      contentRef.current.scrollTo(0, 0);
    }
  }, [pathname]);

  return (
    <Layout>
      <Header>
        <h2>{APP_NAME} / {NETWORK_LOOP.toLocaleLowerCase()}</h2>
        {headerContent}
      </Header>
      <Body>
        <BodyMenu>
          <SideMenu>
            {sideMenuContent}
          </SideMenu>
        </BodyMenu>
        <BodyContent>
          <MaybeBreadcrumbs />
          <Content ref={contentRef}>
            {children}
          </Content>
        </BodyContent>
      </Body>
    </Layout>
  );
};

export default LayoutFC;
