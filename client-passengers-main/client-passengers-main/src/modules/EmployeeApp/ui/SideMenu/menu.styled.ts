import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { lighten } from 'polished';
import { NavLink } from 'react-router-dom';
import styled from 'styled-components';

import { MenuItemType } from './menu.types';

const StyledMenu = styled.nav<{ isExpanded: boolean }>`
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  transition: width 0.5s;
  text-overflow: ellipsis;
  white-space: nowrap;

  width: ${props => (props.isExpanded ? '260px' : '44px')};
`;

const MarkerIndicator = styled.div`
  width: 6px;
  height: 6px;
  display: flex;
  margin: auto;
  border-radius: 50%;
  background-color: rgba(255, 204, 152, 1);
`;

const StyledNavLink = styled(NavLink)<MenuItemType>`
  border-radius: 8px;
  flex-direction: row;
  align-items: center;
  color: #737373;
  stroke: #737373;
  fill: #737373;
  position: relative;
  opacity: ${props => (props.$disable ? '0.6' : '1')};

  pointer-events: ${props => ((props.$hasInnerChilds && props.$isExpanded) || props.$disable ? 'none' : 'auto')};
  margin: ${props => props.$isExpanded && props.$isSubMenu
    ? '0 0 calc(3 * var(--margin-fourth)) var(--margin-large) !important'
    : '0 0 calc(3 * var(--margin-fourth)) 0 !important'};
  display: ${props => (props.$isSubMenu && !props.$isExpanded ? 'none' : 'flex')};

  span {
    font-size: 14px;
    letter-spacing: -0.3px;
    white-space: normal;
    line-height: 22px;
    margin: 0 0 0 calc(3 * var(--margin-fourth));
    max-width: 70%;
  }

  &:hover {
    background-color: white !important;
    color: #737373;
  }

  &.active {
    background-color: white;
    color: var(--jade) !important;
    fill: var(--jade);
    stroke: var(--jade);

    ${MarkerIndicator} {
      width: 12px;
      height: 12px;
      border: 2px solid rgba(242, 243, 246, 0.6);
      background-color: rgba(255, 184, 112, 1);
    }
  }
`;

const Icon = styled(FontAwesomeIcon)`
  margin: 0 10px 0 0;
  font-size: 16px;
`;

const IconContainer = styled.div`
  width: 44px;
  height: 44px;
  display: flex;
  padding: 10px 14px;
  align-items: center;
`;

const Spacer = styled.span`
  flex: 1 1 auto;
`;

const Bager = styled.div<{ $isExpanded?: boolean }>`
  background: var(--jade);
  font-size: 10px;
  letter-spacing: -0.32px;
  border-radius: 10px;
  color: white;
  box-sizing: border-box;
  line-height: 20px;
  margin: 0 0 0 14px;
  font-weight: 600;
  min-height: var(--indent-fourth);

  position: ${props => (props.$isExpanded ? 'inherit' : 'absolute')};
  right: ${props => (props.$isExpanded ? '0' : '5px !important')};
  top: ${props => !props.$isExpanded && '13px'};
  padding: ${props => (props.$isExpanded ? '0 7px' : '0')};
  min-width: ${props => (!props.$isExpanded ? 'var(--indent-fourth)' : '0')};
`;

const Expander = styled.div<{ isRotate: boolean }>`
  display: flex;
  flex-direction: column;
  cursor: pointer;
  fill: #737373;
  justify-content: center;
  align-items: center;
  height: 44px;
  width: 44px;
  margin: 0 0 10px 0;

  transform: ${props => (props.isRotate ? 'rotate(180deg)' : 'rotate(0deg)')};

  &:hover {
    fill: ${lighten(0.2, '#737373')} !important;
  }
`;

const BottomMenu = styled.div`
  display: flex;
  justify-content: space-between;

  a {
    width: 44px;
    margin: 0;
  }
`;

export {
  StyledMenu, StyledNavLink, Icon, IconContainer, Spacer, Bager, Expander, BottomMenu, MarkerIndicator
};
