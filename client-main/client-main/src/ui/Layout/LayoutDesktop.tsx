import * as React from 'react';
import { useLocation } from 'react-router-dom';
import styled from 'styled-components';

import { Breadcrumbs, BreadcrumbsProvider, useBreadcrumbs } from 'shared/components/Breadcrumbs/Breadcrumbs';
import styles from './layout.module.scss';

const StyledHeader = styled.div`
  height: 64px;
  background-color: white;
  flex: 0 0 auto;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 5%);
  z-index: 1;
  padding-left: 24px;
  padding-right: 24px;
  align-items: center;
  display: flex;
  flex-direction: row;
  align-items: center;
`;

const Header: React.FC = ({ children }) => <StyledHeader>{children}</StyledHeader>;

const StyledDrawer = styled.nav`
  flex: 0 0 auto;
  padding: 16px 0px 16px 16px;
`;

const Drawer: React.FC = ({ children }) => <StyledDrawer className="side-menu-container">{children}</StyledDrawer>;

const StyledLayout = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 5%);
`;

const LayoutBody = styled.main`
  width: 100%;
  max-width: 100%;
  display: flex;
  flex-direction: row;
  background-color: #f7f8fa;
  flex: 1 1 auto;
  overflow: hidden;
`;

const Body = styled.main`
  padding: 16px;
  padding-top: 0;
  flex: 1 1 auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
`;

const Content = styled.div`
  flex: 1 1 auto;
  overflow: auto;
`;

const Spacer = styled.div`
  height: 16px;
`;

const MaybeBreadcrumbs = () => {
  const breadcrumbs = useBreadcrumbs();
  return breadcrumbs.breadcrumbs.length > 1 ? <Breadcrumbs /> : <Spacer />;
};

export const LayoutDesktop: React.FC<{ headerContent: React.ReactNode; drawerContent: React.ReactNode }> = ({
  headerContent,
  drawerContent,
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
    <BreadcrumbsProvider>
      <StyledLayout>
        <Header>{headerContent}</Header>
        <LayoutBody>
          <Drawer>{drawerContent}</Drawer>
          <Body>
            <MaybeBreadcrumbs />
            <Content id="scrollContent" ref={contentRef}>
              <div className={styles.pageContent}>
                <div className={styles.container}>{children}</div>
              </div>
            </Content>
          </Body>
        </LayoutBody>
      </StyledLayout>
    </BreadcrumbsProvider>
  );
};
