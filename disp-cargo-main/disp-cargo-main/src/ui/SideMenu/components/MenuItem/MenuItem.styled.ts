import { NavLink } from 'react-router-dom';
import styled from 'styled-components';

import { MenuItemProps } from './MenuItem';

export const MarkerIndicator = styled.div`
  width: 6px;
  height: 6px;
  display: flex;
  margin: auto;
  border-radius: 50%;
  background-color: rgba(255, 204, 152, 1);
`;

export const StyledNavLink = styled(NavLink)<MenuItemProps>`
  border-radius: 8px;
  flex-direction: row;
  align-items: center;
  color: #737373;
  stroke: #737373;
  fill: #737373;
  position: relative;
  opacity: ${props => (props.isDisabled ? '0.6' : '1')};
  padding: 4px;
  justify-content: ${props => (props.isExpanded ? 'flex-start' : 'center')};

  pointer-events: ${props => ((props.hasInnerChild && props.isExpanded) || props.isDisabled ? 'none' : 'auto')};
  margin: ${props => (props.isExpanded ? '0 0 calc(3 * var(--margin-fourth)) 0 !important' : '0px')};
  display: ${props => (props.isSubMenu && !props.isExpanded ? 'none' : 'flex')};

  span {
    font-size: 14px;
    letter-spacing: -0.3px;
    white-space: normal;
    line-height: 22px;
    margin: 0 0 0 8px;
    max-width: 70%;
  }

  &:hover {
    background-color: white !important;
    color: #737373;
  }

  div {
    width: 32px;
    height: 32px;
    display: flex;
    padding: 4px;
    align-items: center;
    display: flex;
    justify-content: center;
    border-radius: 8px;
  }

  &.active {
    background-color: white;
    fill: var(--jade);
    stroke: var(--jade);
    color: ${props => (props.isExpanded && props.isSubMenu ? 'rgb(38, 38, 38)' : 'rgb(19, 22, 24) !important')};
    font-family: SB Sans Text;
    font-size: ${props => (props.isExpanded && props.isSubMenu ? '14px' : '15px')};
    font-weight: ${props => ((props.isExpanded && props.isSubMenu) || props.nonSpoiler) && '600'};
    line-height: 22px;
    letter-spacing: -0.3px;
    margin: ${props => (props.isExpanded ? '0 0 calc(3 * var(--margin-fourth)) 0 !important' : '0 !important')};

    & > div {
      background: ${props => (props.isOpen ? 'none' : 'rgb(231,249,240);')};
    }

    & > div > svg path {
      stroke: ${props => props.isExpanded && props.isSubMenu
    ? 'rgb(16, 191, 106)'
    : props.isExpanded
      ? props.isOpen
        ? 'green'
        : props.nonSpoiler
          ? 'rgb(16, 191, 106)'
          : 'rgb(115, 115, 115)'
      : 'rgb(16, 191, 106)'};
    }

    & > div > svg circle {
      stroke: ${props => (props.isExpanded && props.isSubMenu ? 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)')};
    }

    ${MarkerIndicator} {
      width: 12px;
      height: 12px;
      border: 2px solid rgba(242, 243, 246, 0.6);
      background-color: rgba(255, 184, 112, 1);
    }

    div {
      background: ${props => (props.isExpanded && props.isSubMenu ? '' : 'rgb(231, 249, 240)')};

      img {
        width: ${props => props.isExpanded && props.isSubMenu && '6px'};
      }
    }
  }
`;
