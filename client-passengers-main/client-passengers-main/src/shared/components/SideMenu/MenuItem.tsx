import { Tooltip } from 'antd';
import React, { FC } from 'react';

import { useBadgeCounter } from '../../hooks/useBadgeCounter';
import Arrow from '../Images/direction/Arrow';
import { Bager, IconContainer, StyledNavLink } from './styled';
import { MenuItemType, OnlySpoilerType } from './types';

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
}) => {
  let counts;
  const counters = useBadgeCounter();

  switch (navLinkProps.to) {
    case '/taxi/limits/limitRequests/active':
      counts = counters?.limit;
      break;
    case '/taxi/trips':
      counts = counters?.trips;
      break;
    case '/taxi/approvement/requests/active':
      counts = counters?.requests;
      break;
    case '/taxi/fleetManagement/info':
      counts = counters?.fleetManagement;
      break;
    default:
      counts = 0;
      break;
  }

  return (
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

      {!!counts && <Bager $isExpanded={$isExpanded}>{$isExpanded && counts}</Bager>}

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
};

export { MenuItem, OnlySpoiler, handleStop };
