/* eslint-disable no-unused-expressions */

import React, { useState } from 'react';
import { MenuItems } from '../SideMenu/MenuItems';
import ChevronRight from './images/ChevronRight';
import {
  BottomMenu, Expander, Spacer, StyledMenu
} from './menu.styled';

const SideMenu: React.FC = () => {
  const [isExpanded, expand] = useState(false);

  return (
    <StyledMenu isExpanded={isExpanded} className="side-menu">
      <MenuItems isExpanded={isExpanded} />
      <Spacer />
      <BottomMenu>
        <Expander onClick={() => expand(!isExpanded)} isRotate={isExpanded}>
          <ChevronRight />
        </Expander>
      </BottomMenu>
    </StyledMenu>
  );
};

export default SideMenu;
