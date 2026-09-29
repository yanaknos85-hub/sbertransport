import React, { useState, useEffect, FC } from 'react';
import { NavLink } from 'react-router-dom';
import { Dropdown, Menu } from 'antd';

import useNavigator from 'ui/SideMenu/hooks/useNavigator';
import { IconContainer } from 'ui/SideMenu/SideMenu.styled';
import { MenuItemType } from '../MenuItems/MenuItems';
import MenuItem from '../MenuItem/MenuItem';
import {
  SpoilerStyled, TitleMenu, TitleMenuItems, WrapperInfoMenuItem
} from './Spoiler.styled';

interface SpoilerType {
  item: MenuItemType;
  isExpanded: boolean;
}

const Spoiler: FC<SpoilerType> = ({
  item, isExpanded,
}) => {
  const { checkedActiveMainMenuItem, checkedActiveMenuItem } = useNavigator();
  const [open, setOpen] = useState(false);

  const isActive = checkedActiveMainMenuItem(item.name);

  useEffect(() => {
    !isActive && setOpen(false);
  }, [isActive]);

  const handleOpenSpoiler = () => {
    isExpanded && setOpen(!open);
  };

  return (
    <SpoilerStyled
      onClick={handleOpenSpoiler}
      active={open}
      isExpanded={isExpanded}
      isActive={() => isActive}
    >
      {isExpanded ? (
        <MenuItem
          to={item.path}
          icon={item.icon}
          isActive={() => isActive}
          hasInnerChild
          isOpen={open}
          isExpanded={isExpanded}
        >
          {isExpanded && item.name}
        </MenuItem>
      ) : (
        <Dropdown
          placement="bottomLeft"
          trigger={['click']}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
          overlay={(
            <Menu>
              <TitleMenu>{item.name}</TitleMenu>
              {item.subItems?.map((subItem, idx) => (
                <Menu.Item key={`${subItem.name}_${idx}`}>
                  <NavLink to={subItem.path}>
                    <WrapperInfoMenuItem>
                      <IconContainer>{subItem.icon}</IconContainer>
                      <TitleMenuItems>{subItem.name}</TitleMenuItems>
                    </WrapperInfoMenuItem>
                  </NavLink>
                </Menu.Item>
              ))}
            </Menu>
          )}
        >
          <MenuItem
            to={item.path}
            icon={item.icon}
            isActive={() => isActive}
            hasInnerChild
            isOpen={open}
            isExpanded={isExpanded}
          >
            {isExpanded && item.name}
          </MenuItem>
        </Dropdown>
      )}

      {open && item.subItems?.map((subItem, idx) => (
        <MenuItem
          isActive={() => checkedActiveMenuItem(subItem.name)}
          key={`${subItem.name}_${idx}`}
          to={subItem.path}
          icon={subItem.icon}
          isExpanded={isExpanded}
          isSubMenu
        >
          {subItem.name}
        </MenuItem>
      ))}
    </SpoilerStyled>
  );
};

export default Spoiler;
