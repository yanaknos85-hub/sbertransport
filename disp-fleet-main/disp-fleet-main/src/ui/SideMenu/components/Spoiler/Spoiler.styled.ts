import styled from 'styled-components';

export const SpoilerStyled = styled.div<{ active: boolean; isExpanded: boolean; isActive: () => boolean }>`
  background: ${props => (props.active ? 'white' : '')};
  border-radius: 8px;
  margin-bottom: ${props => (props.active ? '10px' : '')};
  margin: ${props => (props.isExpanded ? (props.active ? '0 0 10px 0' : '0px') : '8px 0 0 0')};

  span {
    font-weight: ${props => (!props.active && props.isActive ? props.isActive() && '600' : '')};
  }

  & > a:first-child > div {
    background: ${props => (props.active ? 'none' : '')};
  }

  & > a:first-child > div > svg path {
    stroke: ${props => !props.active && props.isActive ? props.isActive() && 'rgb(16, 191, 106)' : 'rgb(115, 115, 115)'};
  }

  .ant-dropdown {
    left: 69px !important;

    &-menu {
      padding: 8px 16px !important;
      border-radius: 8px !important;

      .ant-dropdown-menu-item {
        padding: 5px 5px !important;
      }
    }
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
