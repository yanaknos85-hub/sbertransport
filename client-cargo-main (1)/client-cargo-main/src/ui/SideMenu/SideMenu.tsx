import React, { useState } from 'react';

import ChevronRight from './images/ChevronRight';
import { Menu } from './Menu';
import {
  BottomMenu, Expander, Spacer, StyledMenu
} from './menu.styled';

const SideMenu: React.FC = () => {
  const [isExpanded, expand] = useState(false);

  return (
    <StyledMenu isExpanded={isExpanded} className="side-menu">
      <Menu isExpanded={isExpanded} />
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
