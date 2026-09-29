import React, { FC } from 'react';

import { Dropdown, Menu } from 'antd';
import { observer } from 'mobx-react';
import { useRouteMatch } from 'react-router-dom';
import { StoreNames } from 'stores';

import { EmployeeAppLinksTitles, ROLE } from 'constants/constants.app';

import { formatFullName } from 'utils/formatFullName';
import { useRole } from 'utils/useRole';

import { ReactComponent as CorpIcon } from 'shared/components/Images/CorpIcon.svg';
import { ReactComponent as ExitIcon } from 'shared/components/Images/ExitIcon.svg';
import { ReactComponent as ProfileIcon } from 'shared/components/Images/ProfileIcon.svg';
import User from 'shared/components/Images/user.png';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useUrlCorpApp } from 'shared/hooks/useUrlCorpApp';

import styles from './UserBlock.module.scss';

interface UserBlockProps {
  className?: string;
}

const UserBlock: FC<UserBlockProps> = observer(({ className }) => {
  const match = useRouteMatch();
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const { urlOfCorpApp } = useUrlCorpApp();
  const roles = useRole();

  const IS_SDO = configStore.env.IS_SDO;
  const hasCorpAccess = roles.includes(ROLE.EMPLOYEE_CORP_CLIENT);
  const hasCorpMenuItem = (IS_SDO && hasCorpAccess) || !IS_SDO;

  const fullName = formatFullName(
    employeeStore.selfEmployee.firstName,
    employeeStore.selfEmployee.lastName,
    employeeStore.selfEmployee.patronymic
  );

  const menuItems = [
    {
      to: `${match.path}/profile`,
      name: EmployeeAppLinksTitles['profile'],
      image: <ProfileIcon />,
    },
    {
      to: urlOfCorpApp,
      name: EmployeeAppLinksTitles['admin'],
      image: <CorpIcon />,
    },
    {
      to: `/oauth/logout`,
      name: EmployeeAppLinksTitles['exit'],
      image: <ExitIcon />,
    },
  ];

  const handleNavigation = (url: string) => {
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
      {menuItems.map((item, index) => {
        if (!hasCorpMenuItem && item.name === EmployeeAppLinksTitles['admin']) return null;

        if ((item.name !== EmployeeAppLinksTitles['admin'] && IS_SDO) || !IS_SDO) {
          return (
            <Menu.Item key={index}>
              <div
                onClick={() => handleNavigate(item)}
                className={styles.menuItem}
              >
                <div className={styles.img}>{item.image}</div>
                <span>{item.name}</span>
              </div>
            </Menu.Item>
          );
        }
        return null;
      })}
    </Menu>
  );

  return (
    <div className={styles.container}>
      <Dropdown
        placement="bottomRight"
        overlay={menu}
        trigger={['click']}
      >
        <div className={`${styles.userBlock} ${className || ''}`}>
          <span>{fullName}</span>
          <img
            className={styles.avatar}
            src={tripStore.avatar ? tripStore.avatar : User}
            alt="avatar"
          />
        </div>
      </Dropdown>
    </div>
  );
});

export default UserBlock;
