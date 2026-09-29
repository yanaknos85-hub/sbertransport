import React from 'react';

import { useExpander } from './hooks/useExpander';
import { items } from './SideMenu.constants';
import { MenuItems } from './components/MenuItems/MenuItems';
import { ReactComponent as ChevronRight } from 'assets/icons/chevronRight.svg';
import {
  BottomMenu, Expander, Spacer, StyledMenu
} from './SideMenu.styled';

const SideMenu: React.FC = () => {
  const { isExpanded, toggleNav } = useExpander();

  return (
    <StyledMenu isExpanded={isExpanded} className="side-menu">
      <MenuItems isExpanded={isExpanded} items={items} />
      <Spacer />
      <BottomMenu isExpanded={isExpanded} onClick={toggleNav}>
        <Expander isRotate={isExpanded}>
          <ChevronRight />
        </Expander>
        {isExpanded && 'Свернуть меню'}
      </BottomMenu>
    </StyledMenu>
  );
};

export default SideMenu;
