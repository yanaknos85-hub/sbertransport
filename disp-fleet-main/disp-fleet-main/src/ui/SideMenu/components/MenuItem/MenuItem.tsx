import React, { FC, FunctionComponent, SVGProps } from 'react';
import { NavLink } from 'react-router-dom';
import { Tooltip } from 'antd';

import Arrow from 'components/Arrow/Arrow';
import { ignore } from 'utils/utils';
import { IconContainer } from '../../SideMenu.styled';
import { StyledNavLink } from './MenuItem.styled';

export type MenuItemProps = {
  to?: string;
  icon?: JSX.Element | FunctionComponent<SVGProps<SVGSVGElement> & { title?: string }> | string;
  hasInnerChild?: boolean;
  isOpen?: boolean;
  isExpanded?: boolean;
  nonSpoiler?: boolean;
  onClick?(e: React.MouseEvent<HTMLElement, MouseEvent>): void;
} & React.ComponentProps<typeof NavLink>;

const MenuItem: FC<MenuItemProps> = ({
  to = '',
  icon,
  children: name,
  hasInnerChild,
  isOpen,
  isExpanded,
  nonSpoiler,
  onClick = ignore,
  ...navLinkProps
}) => {
  const handleNavLink = event => {
    event.stopPropagation();
  };

  return (
    <StyledNavLink
      {...navLinkProps}
      to={to}
      hasInnerChild={hasInnerChild}
      isExpanded={isExpanded}
      onClick={isExpanded ? handleNavLink : onClick}
      activeClassName="active"
      nonSpoiler={nonSpoiler}
    >
      {!isExpanded ? (
        <Tooltip placement="left" title={name}>
          <IconContainer>{icon}</IconContainer>
        </Tooltip>
      ) : (
        <IconContainer>{icon}</IconContainer>
      )}

      {isExpanded && <span>{name}</span>}

      {hasInnerChild && isExpanded && <Arrow isRotate={isOpen} styles={{ position: 'absolute', right: 10 }} />}
    </StyledNavLink>
  );
};

export default MenuItem;
