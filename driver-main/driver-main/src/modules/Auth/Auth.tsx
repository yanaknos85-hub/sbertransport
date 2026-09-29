import { FC, useMemo } from 'react';
import { Navigate, useLocation } from 'react-router';
import NavBar from 'antd-mobile/es/components/nav-bar';
import { observer } from 'mobx-react';

import { routes } from 'constants/routes.constants';
import { StoreNames } from 'stores/storeNames';
import { useAppStore } from 'stores/stores.context';
import { isNotDirectEnter } from 'utils/routing/isNotRedirectEnter';
import Login from 'components/Basic/Login';

import { ReactComponent as BackArrow } from 'assets/icons/back.svg';
import styles from './Auth.module.scss';

const Auth: FC = observer(() => {
  const { [StoreNames.authStore]: authStore, [StoreNames.configStore]: configStore } = useAppStore();

  const { pathname } = useLocation();

  const { title, subTitle } = useMemo(
    () => ({
      title: authStore.transportPassword ? 'Установите новый\nпароль' : 'Добро пожаловать',
      subTitle: authStore.transportPassword ? '' : 'в Автопарк',
    }),
    [authStore.transportPassword]
  );

  if (authStore.isAuthenticated && isNotDirectEnter(pathname)) {
    return configStore.isBasicAuth ? <Navigate to={routes.Home} /> : <Navigate to={routes.BasicSuccess} />;
  }

  const goBack = () => {
    authStore.transportPassword = false;
  };

  return (
    <div className={styles.auth}>
      <header className={styles.header}>
        {authStore.transportPassword && (
          <NavBar
            className={styles.nav}
            backArrow={<BackArrow />}
            onBack={goBack}
          />
        )}
        <h1>
          {title}
          {subTitle && (
            <>
              <br />
              <span>{subTitle}</span>
            </>
          )}
        </h1>
      </header>
      <main className={styles.content}>
        <Login />
      </main>
      <footer className={styles.footer} />
    </div>
  );
});

export default Auth;
