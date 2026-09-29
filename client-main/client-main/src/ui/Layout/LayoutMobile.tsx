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
  align-items: center;
  display: flex;
  flex-direction: row;
  align-items: center;
`;

const Header: React.FC = ({ children }) => <StyledHeader>{children}</StyledHeader>;

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
  scrollbar-width: thin;
`;

const Spacer = styled.div`
  height: 16px;
`;

const MaybeBreadcrumbs = () => {
  const breadcrumbs = useBreadcrumbs();
  return breadcrumbs.breadcrumbs.length > 1 ? <Breadcrumbs /> : <Spacer />;
};

export const LayoutMobile: React.FC<{ headerContent: React.ReactNode }> = ({ headerContent, children }) => {
  const contentRef = React.useRef<HTMLDivElement>(document.createElement('div'));
  const { pathname } = useLocation();

  React.useEffect(() => {
    if (contentRef.current !== null) {
      if (pathname.includes(`/client/passengers/trips/create/transport2.0`)) {
        contentRef.current.scrollBy(0, 250);
      } else {
        contentRef.current.scrollTo(0, 0);
      }
    }
  }, [pathname]);

  return (
    <BreadcrumbsProvider>
      <StyledLayout>
        <Header>{headerContent}</Header>
        <LayoutBody>
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
