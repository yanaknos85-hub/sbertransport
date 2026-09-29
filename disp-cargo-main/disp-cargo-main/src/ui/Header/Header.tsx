import React, { FC, useCallback } from 'react';
import { ReactComponent as Logo } from 'assets/logo.svg';
import { Link } from 'react-router-dom';

import styles from './header.module.scss';
import { useRole } from 'hooks/useRole';
import { Roles } from 'constants/app.constants';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import { useAutoparkContext } from 'context/Autopark.context';
import Flex from 'components/Flex/Flex';
import { Autopark } from 'api/contractors/contractors.types';

export const Header: FC = () => {
  const roles = useRole();
  const isAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
    || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR);

  const { autoparkId, setAutoparkId } = useAutoparkContext();

  const onFetchAutoparks = useCallback((page: number, data: Autopark[]) => {
    if (page === 0 && !autoparkId) {
      setAutoparkId(data[0]?.id);
    }
  // Чтобы функция не менялась при смене autoparkId. Он все равно актуальный будет
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [setAutoparkId]);

  return (
    <Flex gap={85}>
      <Link to="/">
        <Logo />
        <div className={styles.title}>Автопарки</div>
      </Link>
      {isAdmin && (
        <AutoparkSelect
          value={autoparkId}
          onChange={val => setAutoparkId(String(val))}
          className={styles.autoparkSelect}
          onFetch={onFetchAutoparks}
        />
      )}
    </Flex>
  );
};

export default Header;
