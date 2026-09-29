import * as React from 'react';
import { useLocation } from 'react-router-dom';
import { Drawer } from '@sber-sbertransport/ui-kit/src';
import { useUiContext } from 'shared/components/UI';
import styled from 'styled-components';
import { Menu } from 'ui/SideMenu/Menu';

import Header from '../Header/Header';
import SideMenu from '../SideMenu/SideMenu';

const Layout = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
`;

const MainInner = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
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

  @media (max-width: 767px) {
    display: none;
  }
`;

const BodyContent = styled.main`
  padding: 16px;
  padding-top: 0;
  flex: 1 1 auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;

  @media (max-width: 767px) {
    padding: 0 8px;
  }
`;

const Content = styled.div`
  flex: 1 1 auto;
  overflow: auto;

  ::-webkit-scrollbar {
    display: none;
  }
`;

const Main = styled.div`
  display: flex;
  flex: 1;
  overflow: hidden;
`;

const ContentInner = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const Container = styled.div`
  max-width: 100%;
  width: 100%;
  padding: 0 !important;
  transition: width 0.5s;
`;

const LayoutFC: React.FC<{
  headerContent?: React.ReactNode;
  sideMenuContent?: React.ReactNode;
}> = ({
  headerContent, sideMenuContent, children,
}) => {
  const contentRef = React.useRef<HTMLDivElement>(document.createElement('div'));
  const { pathname } = useLocation();
  const [isNavOpened, setIsNavOpened] = React.useState(false);

  const openNav = () => setIsNavOpened(true);
  const closeNav = () => setIsNavOpened(false);

  const { isMobile } = useUiContext();

  React.useEffect(() => {
    setIsNavOpened(false);
  }, [pathname]);

  React.useEffect(() => {
    if (contentRef.current !== null) {
      contentRef.current.scrollTo(0, 0);
    }
  }, [pathname]);

  return (
    <Layout>
      <Main>
        <MainInner>
          <Header onOpenNav={openNav}>{headerContent}</Header>
          <Body>
            <BodyMenu>
              <SideMenu>{sideMenuContent}</SideMenu>
            </BodyMenu>
            <BodyContent>
              <Content ref={contentRef}>
                <ContentInner>
                  <Container>{children}</Container>
                </ContentInner>
              </Content>
            </BodyContent>
          </Body>
        </MainInner>
      </Main>
      {isMobile && (
        <Drawer
          open={isNavOpened}
          onClose={closeNav}
          placement="left"
        >
          <Menu isExpanded={true} />
        </Drawer>
      )}
    </Layout>
  );
};

export default LayoutFC;
