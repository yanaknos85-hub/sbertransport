/* eslint-disable no-unused-expressions */

import React from 'react';
import styled from 'styled-components';
import { MenuItems } from '../SideMenu/MenuItems';

const StyledMenu = styled.nav`
  display: flex;
  flex-direction: column;
  height: 100%;
`;

const Spacer = styled.span`
  flex: 1 1 auto;
`;

const SideMenu: React.FC = () => {
  return (
    <StyledMenu>
      <MenuItems />
      <Spacer />
    </StyledMenu>
  );
};

export default SideMenu;
