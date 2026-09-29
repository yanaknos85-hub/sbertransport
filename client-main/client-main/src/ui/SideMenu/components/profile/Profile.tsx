import React, { FC, useEffect, useMemo } from 'react';
import {
  FullNameTitle,
  FullNameWrapper,
  IconContainerProfile,
  PositionTitle,
  ProfileTitleMenuItems,
  ProfileWrapper,
  WrapperProfileInfoMenuItem
} from './profile.styled';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatFullName } from 'utils/formatFullName';
import { observer } from 'mobx-react';
import ArrowRight from 'shared/components/Images/chevron-right.svg';
import { AvatarWrapper, UserInfoWrapper } from './profile.styled';
import User from 'shared/components/Images/user.png';
import { NavLink, useRouteMatch } from 'react-router-dom';
import { Dropdown, Menu } from 'antd';
import { EmployeeAppLinksTitles, ROLE } from 'constants/constants.app';
import { ReactComponent as ProfileIcon } from 'shared/components/Images/ProfileIcon.svg';
import { ReactComponent as CorpIcon } from 'shared/components/Images/CorpIcon.svg';
import { ReactComponent as ExitIcon } from 'shared/components/Images/ExitIcon.svg';
import { useUrlCorpApp } from 'shared/hooks/useUrlCorpApp';
import { useRole } from 'utils/useRole';

const Profile: FC<{ isExpanded: boolean; className?: string }> = observer(({ isExpanded, className }) => {
  const match = useRouteMatch();
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.mappedStore]: mappedStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.configStore]: configStore,
    [StoreNames.authStore]: authStore,
  } = useAppStoreContext();
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;
  const fullName = formatFullName(
    employeeStore.selfEmployee.firstName,
    employeeStore.selfEmployee.lastName,
    employeeStore.selfEmployee.patronymic
  );
  const { urlOfCorpApp } = useUrlCorpApp();
  const roles = useRole();

  const IS_SDO = configStore.env.IS_SDO;
  const hasCorpAccess = roles.includes(ROLE.EMPLOYEE_CORP_CLIENT);

  const hasCorpMenuItem = (IS_SDO && hasCorpAccess) || !IS_SDO;

  useEffect(() => {
    tripStore.getUserAvatar(employeeStore.selfEmployee.userId).then(avatar => tripStore.addAvatar(avatar));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [employeeStore.selfEmployee.userId]);

  const checkingPosition = mappedStore.selfEmployeeDetailed.position !== employeeStore.selfEmployee.positionId;

  const urlToCorpClient = useMemo(() => {
    const url = window.location.origin.split('.').slice(1).join('.');

    if (!url || !IS_SDO || !hasCorpAccess) return urlOfCorpApp;

    const finalUrl = new URL(`${window.location.protocol}//client-corp-ext.${url}`);
    finalUrl.searchParams.set('refreshToken', authStore.refreshToken);

    return finalUrl.toString();
  }, [window.location]);

  const menuItems = [
    {
      to: `${match.path}/profile`,
      name: EmployeeAppLinksTitles['profile'],
      image: <ProfileIcon />,
    },
    {
      to: urlToCorpClient,
      name: EmployeeAppLinksTitles['admin'],
      image: <CorpIcon />,
    },
    {
      to: `/oauth/logout`,
      name: EmployeeAppLinksTitles['exit'],
      image: <ExitIcon />,
    },
  ];

  const handleNavigation = url => {
    window.location.href = url;
  };

  const handleNavigate = ({ to, name }: { to: string; name: string }) => {
    const isCorpLinkAvailable = name === EmployeeAppLinksTitles.admin && hasCorpAccess && IS_SDO;

    if (isCorpLinkAvailable || to.includes('https')) {
      handleNavigation(to);
    }
  };

  const menu = (
    <Menu>
      <>
        {menuItems.map((item, index) => {
          if (!hasCorpMenuItem && item.name === EmployeeAppLinksTitles['admin']) return;

          if ((item.name !== EmployeeAppLinksTitles['admin'] && IS_PERSONAL_DEVICE) || !IS_PERSONAL_DEVICE) {
            return (
              <Menu.Item key={index}>
                <NavLink
                  to={{ pathname: item.to }}
                  onClick={() => handleNavigate(item)}
                >
                  <WrapperProfileInfoMenuItem>
                    <IconContainerProfile>{item.image}</IconContainerProfile>
                    <ProfileTitleMenuItems>{item.name}</ProfileTitleMenuItems>
                  </WrapperProfileInfoMenuItem>
                </NavLink>
              </Menu.Item>
            );
          }
        })}
      </>
    </Menu>
  );

  return (
    <Dropdown
      overlayClassName="DropdownMenuProfile"
      placement="bottomCenter"
      overlay={menu}
      trigger={['click']}
    >
      {isExpanded ? (
        <ProfileWrapper className={className}>
          <AvatarWrapper isExpanded={isExpanded}>
            <img src={tripStore.avatar ? tripStore.avatar : User} alt="avatart" />
          </AvatarWrapper>
          <UserInfoWrapper>
            <FullNameWrapper>
              <FullNameTitle>{fullName}</FullNameTitle>
              <img src={ArrowRight} alt="" />
            </FullNameWrapper>
            <PositionTitle>{checkingPosition && mappedStore.selfEmployeeDetailed.position}</PositionTitle>
          </UserInfoWrapper>
        </ProfileWrapper>
      ) : (
        <AvatarWrapper className={className} isExpanded={isExpanded}>
          <img src={tripStore.avatar ? tripStore.avatar : User} alt="avatart" />
        </AvatarWrapper>
      )}
    </Dropdown>
  );
});

export default Profile;
