import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { lighten } from 'polished';
import { NavLink } from 'react-router-dom';
import styled from 'styled-components';

import { MenuItemType } from './types';

const StyledMenu = styled.nav<{ isExpanded: boolean }>`
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: width 0.5s;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: ${props => (props.isExpanded ? 'auto' : 'hidden')};

  width: ${props => (props.isExpanded ? '260px' : '44px')};
  min-width: ${props => (props.isExpanded ? '260px' : '44px')};

  .ant-divider {
    width: ${props => (props.isExpanded ? '90% !important' : '100% !important')};
    margin: ${props => (props.isExpanded ? '10px 0px 10px 10px !important' : '10px 0px 10px 0px !important')};
    min-width: 90% !important;
  }
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
  padding:4px;
  justify-content: ${props => (props.$isExpanded ? 'flex-start' : 'center')};

  pointer-events: ${props => ((props.$hasInnerChilds && props.$isExpanded) || props.$disable ? 'none' : 'auto')};
  margin: ${props => (props.$isExpanded ? '0 0 calc(3 * var(--margin-fourth)) 0 !important' : '0px')};
  display: ${props => (props.$isSubMenu && !props.$isExpanded ? 'none' : 'flex')};

  span {
    font-size: 14px;
    letter-spacing: -0.3px;
    white-space: ${props => props.$noWhiteSpaceNormal ? 'nowrap' : 'normal'};
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
    color: ${props => props.$isExpanded && props.$isSubMenu ? 'rgb(38, 38, 38)' : 'rgb(19, 22, 24) !important'};
    font-family: SB Sans Text;
    font-size: ${props => props.$isExpanded && props.$isSubMenu ? '14px' : '15px'};
    font-weight: ${props => ((props.$isExpanded && props.$isSubMenu) || props.nonSpoiler) && '600'};
    line-height: 22px;
    letter-spacing: -0.3px;
    margin: ${props => props.$isExpanded ? '0 0 calc(3 * var(--margin-fourth)) 0 !important' : '0 !important'};
    
    & > div {
      background: ${props => props.isOpen ? 'none' : 'rgb(231,249,240);'};
    }

    & > div > svg path {
      stroke: ${props => props.$isExpanded && props.$isSubMenu ? 'rgb(16, 191, 106)' : props.$isExpanded ? props.isOpen ? 'green' : props.nonSpoiler ? 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)' : 'rgb(16, 191, 106)'};
    }

    & > div > svg circle {
      stroke: ${props => props.$isExpanded && props.$isSubMenu ? 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)'};
    }

    ${MarkerIndicator} {
      width: 12px;
      height: 12px;
      border: 2px solid rgba(242, 243, 246, 0.6);
      background-color: rgba(255, 184, 112, 1);
    }

    div {
      background: ${props => props.$isExpanded && props.$isSubMenu
    ? ''
    : 'rgb(231, 249, 240)'};

      img {
        width: ${props => props.$isExpanded && props.$isSubMenu && '6px'};
      }
    }
  }
`;

const Icon = styled(FontAwesomeIcon)`
  margin: 0 10px 0 0;
  font-size: 16px;
`;

const IconContainer = styled.div`
  width: 32px;
  height: 32px;
  display: flex;
  padding: 4px;
  align-items: center;
  display: flex;
  justify-content: center;
  border-radius: 8px;
  
  &.active {
    background: rgb(231, 249, 240);
  }

  svg {

    path {
      stroke: rgb(115, 115, 115);
    }
  }
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
  width: 44px;

  transform: ${props => (props.isRotate ? 'rotate(180deg)' : 'rotate(0deg)')};

  &:hover {
    fill: ${lighten(0.2, '#737373')} !important;
  }
`;

const BottomMenu = styled.div<{ isExpanded: boolean }>`
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 16px;
  border-radius: 12px;
  background: rgb(242, 243, 246);
  color: rgb(115, 115, 115);
  font-family: SB Sans Text;
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  cursor: pointer;
  height: ${props => (props.isExpanded ? '' : '44px')};
  width: ${props => (props.isExpanded ? '' : '44px')};

  a {
    width: 44px;
    margin: 0;
  }
`;

export const Spoiler = styled.div<{ active: boolean; isExpanded: boolean; isActive: () => boolean }>`
  background: ${props => (props.active ? 'white' : '')};
  border-radius: 8px;
  margin-bottom: ${props => (props.active ? '10px' : '')};
  margin: ${props => (props.isExpanded ? props.active ? '0 0 10px 0' : '0px' : '8px 0 0 0')};

  span {
    font-weight: ${props => (!props.active && props.isActive ? props.isActive() && '600' : '')};
  }

  & > a:first-child > div {
    background: ${props => (props.active ? 'none' : '')};
  }

  & > a:first-child > div > svg path {
    stroke: ${props => (!props.active && props.isActive ? props.isActive() && 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)')};
    // stroke: ${props => (props.active ? 'rgb(115, 115, 115)' : props.isActive ? props.isActive() && 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)')};
  }
`;

export const TitleMenuItems = styled.span`
  color: rgb(115, 115, 115);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
  margin-left: 16px;
`;

export const WrapperInfoMenuItem = styled.div`
  display: flex;
  align-items: center;
`;

export const TitleMenu = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export {
  StyledMenu, StyledNavLink, Icon, IconContainer, Spacer, Bager, Expander, BottomMenu, MarkerIndicator
};
