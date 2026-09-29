import { observer } from 'mobx-react';
import React, { FC, useCallback } from 'react';

import { useVehicles } from 'api/vehicles';

import * as routes from 'constants/constants.routes';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import Vehicle from './components/Vehicle/Vehicle';

import styles from './vehiclesList.module.scss';

const VehiclesList: FC = observer(() => {
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const { selfEmployee } = employeeStore;

  const { data = [] } = useVehicles(selfEmployee);

  const goAdd = useCallback(() => configStore.history.push(routes.VEHICLES_ADD), [history]);

  return (
    <>
      <div className={styles.title}>Мой транспорт</div>
      <div className={styles.list}>
        {data.map(item => (
          <div className={styles.item} key={item.id}>
            <Vehicle {...item} />
          </div>
        ))}
        <div className={styles.itemAdd} onClick={goAdd}>
          + Добавить транспорт
        </div>
      </div>
    </>
  );
});

const ErrorBoundaryWrapper: FC = ({ children }) => (
  <ErrorBoundary>
    <React.Suspense fallback={<SpinWrapped />}>{children}</React.Suspense>
  </ErrorBoundary>
);

export const VehicleListAsPage = () => (
  <ErrorBoundaryWrapper>
    <div className={styles.container}>
      <div className={styles.viewport}>
        <div className={styles.content}>
          <VehiclesList />
        </div>
      </div>
    </div>
  </ErrorBoundaryWrapper>
);

export default function VehicleListAsPartial() {
  return (
    <ErrorBoundaryWrapper>
      <VehiclesList />
    </ErrorBoundaryWrapper>
  );
}
