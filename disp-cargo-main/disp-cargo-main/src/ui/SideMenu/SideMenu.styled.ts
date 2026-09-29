import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { lighten } from 'polished';
import styled from 'styled-components';

export const StyledMenu = styled.nav<{ isExpanded: boolean }>`
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

export const Spacer = styled.span`
  flex: 1 1 auto;
`;

export const BottomMenu = styled.div<{ isExpanded: boolean }>`
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

export const Expander = styled.div<{ isRotate: boolean }>`
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

export const IconContainer = styled.div`
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

export const Icon = styled(FontAwesomeIcon)`
  margin: 0 10px 0 0;
  font-size: 16px;
`;
