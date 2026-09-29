import React, { FC } from 'react';
import styled from 'styled-components';
import { useHistory } from 'react-router-dom';
import { ReactComponent as Logo } from 'shared/images/logo.svg';

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

const LogoWrapper = styled.div`
  cursor: pointer;
`;

const Spacer = styled.div`
  flex-grow: 1;
`;

export const HeaderDesktop: FC = () => {
  const history = useHistory();

  return (
    <StyledHeader>
      <LogoWrapper onClick={() => { history.push('/'); }}>
        <Logo />
      </LogoWrapper>
      <Spacer />
    </StyledHeader>
  );
};

