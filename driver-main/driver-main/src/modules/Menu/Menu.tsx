import { FC } from 'react';
import { useNavigate } from 'react-router';
import { List } from 'antd-mobile';

import { useContractor, useProfile, useUserAvatar } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import NavBar from 'components/NavBar';
import Avatar from 'components/Avatar/Avatar';
import PhoneConfirmationAlert from 'components/PhoneConfirmationAlert';
import { getFullName } from 'utils/formattors/getFullName';

import { menuItems } from './items';
import { ReactComponent as ChevronIcon } from 'assets/icons/chevron-right.svg';
import { ReactComponent as ArrowIcon } from 'assets/icons/arrow.svg';
import styles from './Menu.module.scss';

const Menu: FC = () => {
  const navigate = useNavigate();

  const driver = useProfile().data;
  const avatarUrl = useUserAvatar(driver.id).data;
  const contactor = useContractor(driver.contractorId).data;

  const onNavigate = (path?: string) => () => {
    path && navigate(path);
  };

  const items = menuItems(contactor?.mainDispatcher?.phone);

  return (
    <div className={styles.menu}>
      <NavBar onBack={onNavigate(routes.Home)} />
      <div className={styles.profile}>
        <div className={styles.info}>
          <Avatar src={avatarUrl} size={81} />
          <div className={styles.user} onClick={onNavigate(routes.Profile)}>
            <span className={styles.userName}>{getFullName(driver)}</span>
            <span className={styles.userRole}>Водитель</span>
            <ChevronIcon className={styles.icon} />
          </div>
        </div>
        {!driver.phoneConfirmed && (
          <PhoneConfirmationAlert className={styles.warning} />
        )}
      </div>

      <div className={styles.items}>
        {items.map(section => (
          <List key={section.sectionTitle} header={section.sectionTitle}>
            {section.items.map((item, idx) => (
              <List.Item
                key={`${idx}-${item.title}`}
                title={item.title}
                prefix={item.icon}
                disabled={item.disabled}
                arrow={item.arrow ? <ArrowIcon /> : null}
                onClick={onNavigate(item.link)}
              >
                {item.children}
              </List.Item>
            ))}
          </List>
        ))}
      </div>
    </div>
  );
};

export default Menu;
