import React, { FC } from 'react';
import { Tooltip } from 'antd';

import { MenuItemType, OnlySpoilerType } from './menu.types';
import { IconContainer, StyledNavLink } from './menu.styled';
import Arrow from './images/Arrow';

const OnlySpoiler: FC<OnlySpoilerType> = ({
  setOpen, open, children, $isExpanded,
}) => (
  // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
  <div onClick={() => $isExpanded && setOpen(!open)} className="spoiler">
    {children}
  </div>
);

const handleStop = (event: React.MouseEvent<HTMLElement, MouseEvent>): void => {
  event.stopPropagation();
};

const MenuItem: FC<MenuItemType> = ({
  icon,
  children: name,
  $hasInnerChilds: $innerChilds,
  isOpen,
  $isExpanded,
  onClick,
  ...navLinkProps
}): JSX.Element => (
  <StyledNavLink
    $hasInnerChilds={$innerChilds}
    $isExpanded={$isExpanded}
    onClick={onClick}
    {...navLinkProps}
    activeClassName="active"
  >
    {!$isExpanded ? (
      <Tooltip placement="left" title={name}>
        <IconContainer>{icon}</IconContainer>
      </Tooltip>
    ) : (
      <IconContainer>{icon}</IconContainer>
    )}

    <span>{name}</span>

    {$innerChilds && $isExpanded && (
    <Arrow
      isRotate={isOpen}
      styles={{
        position: 'absolute', right: 10, top: 10,
      }}
    />
    )}
  </StyledNavLink>
);

export { MenuItem, OnlySpoiler, handleStop };
