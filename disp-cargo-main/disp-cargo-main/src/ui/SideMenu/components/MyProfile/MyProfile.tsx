import React, { FC } from 'react';
import { NavLink } from 'react-router-dom';
import { observer } from 'mobx-react';
import { Dropdown, Menu } from 'antd';

import { useProfile } from 'api/profile/profile.api';
import { formatFullName } from 'utils/formatFullName';
import { useRole } from 'hooks/useRole';
import { Roles } from 'constants/app.constants';

import ArrowRight from 'assets/icons/chevronRight.svg';
import User from 'assets/images/user.png';
import { ReactComponent as ExitIcon } from 'assets/icons/ExitIcon.svg';

import {
  AvatarWrapper,
  DropdownWrapper,
  FullNameTitle,
  FullNameWrapper,
  IconContainerProfile,
  PositionTitle,
  ProfileTitleMenuItems,
  ProfileWrapper,
  UserInfoWrapper,
  WrapperProfileInfoMenuItem
} from './MyProfile.styled';

const Profile: FC<{ isExpanded: boolean }> = observer(({ isExpanded }) => {
  const profile = useProfile().data;

  const roles = useRole();
  const isAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
    || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR);

  const fullName = formatFullName(
    profile.firstName,
    profile.lastName,
    profile.patronymic
  );

  // useEffect(() => {
  //   tripStore.getUserAvatar(employeeStore.selfEmployee.userId).then(avatar => tripStore.addAvatar(avatar));
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, [employeeStore.selfEmployee.userId]);

  const menuItems = [
    {
      to: `/oauth/logout`,
      name: 'Выход',
      image: <ExitIcon />,
    },
  ];

  const menu = (
    <Menu>
      <>
        {menuItems.map((item, index) => (
          <Menu.Item key={index}>
            <NavLink to={{ pathname: item.to }}>
              <WrapperProfileInfoMenuItem>
                <IconContainerProfile>{item.image}</IconContainerProfile>
                <ProfileTitleMenuItems>{item.name}</ProfileTitleMenuItems>
              </WrapperProfileInfoMenuItem>
            </NavLink>
          </Menu.Item>
        ))}
      </>
    </Menu>
  );

  return (
    <DropdownWrapper>
      <Dropdown
        placement="bottomCenter"
        overlay={menu}
        trigger={['click']}
        getPopupContainer={trigger => trigger.parentNode as HTMLElement}
      >
        {isExpanded
          ? (
            <ProfileWrapper>
              <AvatarWrapper isExpanded={isExpanded}>
                <img src={User} alt="avatar" />
              </AvatarWrapper>
              <UserInfoWrapper>
                <FullNameWrapper>
                  <FullNameTitle>
                    {fullName}
                  </FullNameTitle>
                  <img src={ArrowRight} alt="" />
                </FullNameWrapper>
                <PositionTitle>
                  {isAdmin ? 'Администратор' : 'Диспетчер'}
                </PositionTitle>
              </UserInfoWrapper>
            </ProfileWrapper>
          )
          : (
            <AvatarWrapper isExpanded={isExpanded}>
              <img src={User} alt="avatar" />
            </AvatarWrapper>
          )}
      </Dropdown>
    </DropdownWrapper>
  );
});

export default Profile;
