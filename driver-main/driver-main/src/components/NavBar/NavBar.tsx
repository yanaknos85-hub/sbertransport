import { FC } from 'react';
import { useNavigate } from 'react-router';
import AntdNavBar, { NavBarProps } from 'antd-mobile/es/components/nav-bar';
import cn from 'classnames';

import { ReactComponent as BackArrow } from 'assets/icons/back.svg';
import styles from './NavBar.module.scss';

const NavBar: FC<NavBarProps> = ({
  className, onBack, ...props
}) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate(-1);
    }
  };

  return (
    <AntdNavBar
      backArrow={<BackArrow />}
      {...props}
      className={cn(styles.navBar, [className])}
      onBack={handleBack}
    />
  );
};

export default NavBar;
