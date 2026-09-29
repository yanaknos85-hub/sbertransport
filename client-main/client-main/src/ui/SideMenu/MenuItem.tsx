import { Tooltip } from 'antd';
import React, { FC, useEffect } from 'react';

import Arrow from 'shared/components/Images/direction/Arrow';
import { IconContainer, Spoiler, StyledNavLink } from './styled';
import { MenuItemType, OnlySpoilerType } from './types';
// import {useBadgeCounter} from '../../hooks/useBadgeCounter';

const OnlySpoiler: FC<OnlySpoilerType> = (
  {
    setOpen,
    open,
    children,
    $isExpanded,
    handleOpen,
    setMenuItems,
    typeMenu,
    setTitleTypeMenuItems,
    titleMenu,
    isActive,
  }) => {
  useEffect(() => {
    handleOpen && open && handleOpen();
  }, [open]);

  const handleOpenSpoiler = () => {
    $isExpanded && setOpen(!open);
    setMenuItems(typeMenu);
    setTitleTypeMenuItems(titleMenu);
  };

  return (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
    <Spoiler
      onClick={handleOpenSpoiler}
      active={open}
      isExpanded={$isExpanded}
      isActive={isActive}
    >
      {children}
    </Spoiler>
  );
};

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
  handleOpen,
  open,
  setOpen,
  nonSpoiler,
  ...navLinkProps
}) => {
  // const counts = 0;
  // Выпилено TRANSPORT-12504
  // const counters = useBadgeCounter();

  // switch (navLinkProps.to) {
  //   case '/taxi/limits/limitRequests/active':
  //     counts = counters?.limit;
  //     break;
  //   case '/taxi/trips':
  //     counts = counters?.trips;
  //     break;
  //   case '/taxi/approvement/requests/active':
  //     counts = counters?.requests;
  //     break;
  //   case '/taxi/fleetManagement/info':
  //     counts = counters?.fleetManagement;
  //     break;
  //   default:
  //     counts = 0;
  //     break;
  // }

  useEffect(() => {
    handleOpen && open && handleOpen();
  }, [open]);

  const handleNavLink = event => {
    !$isExpanded && setOpen(!open);
    onClick(event);
    event.stopPropagation();
  };

  return (
    <StyledNavLink
      $hasInnerChilds={$innerChilds}
      $isExpanded={$isExpanded}
      onClick={$isExpanded ? event => handleNavLink(event) : onClick}
      {...navLinkProps}
      activeClassName="active"
      nonSpoiler={nonSpoiler}
    >
      {!$isExpanded ? (
        <Tooltip placement="left" title={name}>
          <IconContainer>{icon}</IconContainer>
        </Tooltip>
      ) : (
        <IconContainer>{icon}</IconContainer>
      )}

      {$isExpanded
      && <span>{name}</span>}

      {/* {!!counts && <Bager $isExpanded={$isExpanded}>{$isExpanded && counts}</Bager>} */}

      {$innerChilds && $isExpanded && <Arrow isRotate={isOpen} styles={{ position: 'absolute', right: 10 }} />}
    </StyledNavLink>
  );
};

export { MenuItem, OnlySpoiler, handleStop };
