import React, { FC } from 'react';
import styled from 'styled-components';
import { useHistory } from 'react-router-dom';
import { ReactComponent as Logo } from 'shared/images/logo.svg';

const LogoWrapper = styled.div`
  cursor: pointer;
`;

const Spacer = styled.div`
  flex-grow: 1;
`;

const Header: FC = () => {
  const history = useHistory();

  return (
    <>
      <LogoWrapper onClick={() => { history.push('/'); }}>
        <Logo />
      </LogoWrapper>
      <Spacer />
    </>
  );
};

export default Header;
