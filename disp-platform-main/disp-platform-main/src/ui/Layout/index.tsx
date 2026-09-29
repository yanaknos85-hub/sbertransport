import React from 'react';
import { useLocation } from 'react-router-dom';
import { MaybeBreadcrumbs } from 'components/Breadcrumbs/Breadcrumbs';
import * as S from './Layout.styled';
import { APP_NAME, NETWORK_LOOP } from 'constants/env.constants';

const Layout: React.FC<{ headerContent: React.ReactNode; drawerContent: React.ReactNode }> = ({
  headerContent,
  drawerContent,
  children,
}) => {
  const contentRef = React.useRef<HTMLDivElement>(document.createElement('div'));
  const { pathname } = useLocation();

  React.useEffect(() => {
    // Reset scroll position to top on every route change
    if (contentRef.current !== null) {
      contentRef.current.scrollTo(0, 0);
    }
  }, [pathname]);

  return (
    <S.Layout>
      <S.Header>
        {headerContent}
        <h2>
          <span>MF: </span>
          <span>{APP_NAME}</span>
          <span> / BACK: </span>
          <span>{NETWORK_LOOP.toLocaleLowerCase()}</span>
        </h2>
        <div></div>
      </S.Header>
      <S.LayoutBody>
        <S.Drawer>{drawerContent}</S.Drawer>
        <S.Body>
          <MaybeBreadcrumbs />
          <S.Content ref={contentRef}>{children}</S.Content>
        </S.Body>
      </S.LayoutBody>
    </S.Layout>
  );
};

export default Layout;
