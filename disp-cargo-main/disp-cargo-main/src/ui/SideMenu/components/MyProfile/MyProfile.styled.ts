import styled from 'styled-components';

export const DropdownWrapper = styled.div`
  .ant-dropdown {
    left: 16px !important;

    &-menu {
      padding: 8px 16px !important;
      border-radius: 8px !important;

      .ant-dropdown-menu-item {
        padding: 5px 5px !important;
      }
    }
  }
`;

export const ProfileWrapper = styled.div`
  display: flex;
  border-radius: 12px;
  background: rgb(16, 191, 106);
  margin: 8px 0 36px 0;
  padding: 12px;
  cursor: pointer;
`;

export const AvatarWrapper = styled.div<{ isExpanded?: boolean }>`
  display: flex;
  border-radius: 50%;
  width: ${props => (props.isExpanded ? '45px' : '48px')};
  height: ${props => (props.isExpanded ? '36px' : '48px')};
  overflow: hidden;
  border: 2px solid white;
  position: relative;
  left: ${props => (props.isExpanded ? '0px' : '-2px')};
  margin: ${props => (props.isExpanded ? '0px' : '20px 0px 44px 0px')};

  img {
    height: ${props => (props.isExpanded ? '36px' : '48px')};
    width: ${props => (props.isExpanded ? '33px' : '48px')};
    border-radius: 50%;
    object-fit: cover;
    cursor: pointer;
  }
`;

export const UserInfoWrapper = styled.div`
  display: flex;
  flex-direction: column;
  margin-left: 12px;
  width: 100%;
}
`;

export const PositionTitle = styled.span`
  color: rgb(231, 249, 240);
  font-family: SB Sans Text;
  font-size: 12px;
  font-weight: 400;
  line-height: 19px;
  letter-spacing: -0.3px;
`;

export const FullNameWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
`;

export const FullNameTitle = styled.span`
  color: rgb(255, 255, 255);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
`;

export const WrapperProfileInfoMenuItem = styled.div`
  display: flex;
  align-items: center;
`;

export const IconContainerProfile = styled.div`
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
`;

export const ProfileTitleMenuItems = styled.span`
  color: rgb(115, 115, 115);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
  margin-left: 16px;
`;
