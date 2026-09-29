import { FC, Suspense } from 'react';
import { Popup } from 'antd-mobile';
import { Outlet, useLocation, useNavigate } from 'react-router';
import { ReactComponent as Burger } from 'assets/icons/burger.svg';
import { routes } from 'constants/routes.constants';
import styles from './Nav.module.scss';

const Nav: FC = () => {
  const { pathname } = useLocation();
  const navigate = useNavigate();

  const isNavOpened = new RegExp(`^${routes.Nav}(/.*)?$`).test(pathname);

  return (
    <>
      <div className={styles['nav-button']} onClick={() => navigate(routes.Nav)}>
        <Burger />
      </div>

      <Popup
        position="left"
        visible={isNavOpened}
        bodyStyle={{ width: '100vw' }}
      >
        <Suspense>
          {isNavOpened && <Outlet />}
        </Suspense>
      </Popup>
    </>
  );
};

export default Nav;
