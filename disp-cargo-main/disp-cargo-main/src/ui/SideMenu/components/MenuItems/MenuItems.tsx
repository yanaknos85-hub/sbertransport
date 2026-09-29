import React, { useMemo, useState } from 'react';
import { Router, RouterProps } from 'react-router-dom';
import { StoreNames, useAppStore } from 'ioc';

import { AppTitles } from 'constants/app.constants';
import Profile from '../MyProfile/MyProfile';
import Spoiler from '../Spoiler/Spoiler';
import MenuItem from '../MenuItem/MenuItem';
import useNavigator from '../../hooks/useNavigator';

export interface MenuItemType {
  path?: string;
  name: string;
  icon: JSX.Element;
  subItems?: {
    path: string;
    name: AppTitles;
    icon: JSX.Element;
  }[];
}

interface MenuItemsProps {
  items: MenuItemType[];
  isExpanded: boolean;
}

export const MenuItems: React.FC<MenuItemsProps> = ({ items, isExpanded }) => {
  const { checkedActiveMenuItem } = useNavigator();
  const { [StoreNames.configStore]: configStore } = useAppStore();
  const { history } = configStore;

  const [getHistory, setHistory] = useState<RouterProps>({ history: {} } as RouterProps);

  useMemo((): void => {
    setHistory({ history });
  }, [history]);

  return (
    <Router history={getHistory.history}>
      <Profile isExpanded={isExpanded} />
      {items.map((item: MenuItemType, idx) => {
        if (item.subItems) {
          return (
            <Spoiler
              key={`spoiler_${item.name}_${idx}`}
              isExpanded={isExpanded}
              item={item}
            />
          );
        }

        return (
          <MenuItem
            isActive={() => checkedActiveMenuItem(item.name)}
            key={`menu_item_${item.name}_${idx}`}
            to={item.path}
            icon={item.icon}
            isExpanded={isExpanded}
            nonSpoiler
          >
            {isExpanded && item.name}
          </MenuItem>
        );
      })}
    </Router>
  );
};

export { MenuItems as Menu };
