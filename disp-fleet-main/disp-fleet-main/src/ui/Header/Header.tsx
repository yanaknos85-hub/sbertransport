import React, {
  FC, useCallback, useEffect, useMemo
} from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { Autopark } from 'api/contractors/contractors.types';
import { SEARCH_FILTERS_SETTING } from 'constants/app.constants';
import Flex from 'components/Flex/Flex';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { useAutoparkContext } from 'context/Autopark.context';
import { useBranchContext } from 'context/Branch.context';
import { useRoleMap } from 'hooks/useRoleMap';

import { ReactComponent as Logo } from 'assets/logo.svg';
import styles from './header.module.scss';

export const Header: FC = () => {
  const { t } = useTranslation();
  const { isAdmin, isManager } = useRoleMap();

  const { contractorId, autoparkId } = useProfile().data;

  const { autoparkId: savedAutoparkId, setAutoparkId } = useAutoparkContext();
  const { setBranchId } = useBranchContext();

  const searchFilters = useMemo(() => {
    const filters = JSON.parse(localStorage.getItem(SEARCH_FILTERS_SETTING) ?? 'null');
    localStorage.removeItem(SEARCH_FILTERS_SETTING);

    return filters;
  }, []);

  useEffect(() => {
    if (searchFilters) {
      if (isAdmin) {
        setAutoparkId(searchFilters.autoparkId);
        setBranchId(searchFilters.branchId);
        return;
      }

      if (isManager) {
        setAutoparkId(contractorId);
        setBranchId(searchFilters.autoparkId === contractorId ? searchFilters.branchId : undefined);
      }
    } else if (isManager) {
      setAutoparkId(contractorId);

      if (autoparkId && savedAutoparkId !== contractorId) {
        setBranchId(undefined);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onFetchAutoparks = useCallback((page: number, data: Autopark[]) => {
    if (page === 0 && !contractorId && !searchFilters) {
      setAutoparkId(data[0]?.id);
    }
  // Чтобы функция не менялась при смене autoparkId. Он все равно актуальный будет
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [setAutoparkId]);

  const onAutoparkId = useCallback(val => {
    setAutoparkId(String(val));
    setBranchId(undefined);
  }, [setAutoparkId, setBranchId]);

  return (
    <Flex gap={85}>
      <Link to="/">
        <Logo />
        <div className={styles.title}>Автопарки</div>
      </Link>
      <div>
        {(isAdmin || isManager) && (
          <AutoparkSelect
            value={contractorId}
            onChange={onAutoparkId}
            className={styles.select}
            onFetch={isAdmin ? onFetchAutoparks : undefined}
            disabled={!isAdmin}
          />
        )}
        {((isAdmin || isManager) || (autoparkId && autoparkId !== 'null')) && (
          <BranchSelect
            value={autoparkId === 'null' ? undefined : autoparkId}
            autoparkId={contractorId}
            onChange={setBranchId}
            className={styles.select}
            placeholder={t.Autoparks.allBranches}
            disabled={!(isAdmin || isManager)}
            allowClear
          />
        )}
      </div>
    </Flex>
  );
};
export default Header;
