import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { SpinWrapped } from '../../shared/components';
import styles from './assets/styles/Bonuses.module.scss';
import { FirstBloc } from './Layouts/Account/FirstBloc';
import { SecondBloc } from './Layouts/Account/SecondBloc';

export const AccountPage: FC = observer(() => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  useEffect(() => {
    limitsStore.getAccountBonuses();
  }, [limitsStore]);

  return !limitsStore.bonuses ? (
    <SpinWrapped />
  ) : (
    <div className={styles.container}>
      <FirstBloc />
      <SecondBloc bonuses={limitsStore.bonuses} />
    </div>
  );
});
