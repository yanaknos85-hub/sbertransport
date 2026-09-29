import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import styled from 'styled-components';
import { useHistory, useLocation } from 'react-router-dom';
import { ReactComponent as Logo } from 'shared/images/logo.svg';
import { MenuMobile } from './MenuMobile';
import Profile from 'ui/SideMenu/components/profile/Profile';
import { ReactComponent as Hamburger } from 'shared/icons/hamburger.svg';
import { ReactComponent as HamburgerClose } from 'shared/icons/hamburger-close.svg';

const ProfileMobile = styled(Profile)`
  & {
    margin: 0;
    min-width: 48px;
  }
`;

const StyledHeader = styled.div`
  width: 100%;
  height: 64px;
  background-color: white;
  flex: 0 0 auto;
  z-index: 1;
  gap: 16px;
  padding-left: 16px;
  padding-right: 16px;
  align-items: center;
  display: flex;
  flex-direction: row;
  align-items: center;
`;

const LogoWrapper = styled.div`
  cursor: pointer;
  display: flex;
`;

const Spacer = styled.div`
  flex-grow: 1;
`;

const ButtonIcon = styled.button<{ close: boolean }>`
  background: none;
  color: inherit;
  padding: 0;
  outline: inherit;

  width: 42px;
  height: 42px;
  display: flex;

  align-items: center;
  justify-content: center;

  border: ${({ close }) => (close ? '2px solid #00000080' : 'none')};
  border-radius: ${({ close }) => (close ? '22px' : 0)};
`;

const HamburgerStyled = styled(Hamburger)`
  width: 22px;
  height: 22px;
  color: #00000080;
`;

const HamburgerCloseStyled = styled(HamburgerClose)`
  width: 22px;
  height: 22px;
  color: #00000080;
`;

interface Props { drawerContent: React.ReactNode }

export const HeaderMobile: FC<Props> = ({ drawerContent }) => {
  const history = useHistory();

  const location = useLocation();

  const [showMenu, setShowMenu] = useState(false);

  useEffect(() => {
    setShowMenu(false);
  }, [location?.pathname]);

  const toggleShowMenu = useCallback(() => {
    setShowMenu(prev => !prev);
  }, []);

  const closeMenu = useCallback(() => {
    setShowMenu(false);
  }, []);

  return (
    <>
      <StyledHeader>
        <ButtonIcon close={showMenu} onClick={toggleShowMenu}>
          {showMenu ? <HamburgerCloseStyled /> : <HamburgerStyled />}
        </ButtonIcon>

        <LogoWrapper
          onClick={() => {
            history.push('/');
          }}
        >
          <Logo />
        </LogoWrapper>
        <Spacer />

        <ProfileMobile isExpanded={false} />
      </StyledHeader>

      {showMenu && (
        <MenuMobile
          onClose={closeMenu}
          topOffset={64}
        >
          {drawerContent}
        </MenuMobile>
      )}
    </>
  );
};
