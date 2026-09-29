import { useHistory as History } from '@sber-sbertransport/mf-core';
import { observer } from 'mobx-react';
import React, {
  FC, useCallback, useEffect
} from 'react';

import { StoreNames } from 'stores/StoreNames.enum';

import { useVehicles } from 'api/vehicles';

import * as routes from 'constants/constants.routes';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import AddTransport from 'shared/components/Images/AddTransport.svg';
import { ReactComponent as AddIcon } from 'shared/components/Images/AddIcon.svg';
import { ReactComponent as BlackIconAdd } from 'shared/components/Images/blackIconAdd.svg';
import Vehicle from './components/Vehicle/Vehicle';

import styles from './vehiclesList.module.scss';

export const VehiclesList: FC = observer(() => {
  const history = History();
  const { [StoreNames.employeeStore]: employeeStore, [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const { selfEmployee } = employeeStore;
  const { data = [] } = useVehicles(selfEmployee);

  const goAdd = useCallback(() => history.push(routes.VEHICLES_ADD), [history]);

  useEffect(() => {
    if (tripStore.vehicles !== data && data.length > 0) {
      tripStore.setVehicles(data);
    }
  }, [data]);

  return (
    <>
      {tripStore.vehicles.length ? (
        <div className={styles.wrapperVehiclesList}>
          <div className={styles.title}>Мой транспорт</div>
          <div className={styles.list}>
            {tripStore.vehicles.map(item => (
              <div className={styles.item} key={item.id}>
                <Vehicle {...item} />
              </div>
            ))}
            <div className={styles.itemAdd} onClick={goAdd}>
              <BlackIconAdd />
              Добавить транспорт
            </div>

          </div>
        </div>
      )
        : (
          <div className={styles.wrapperAddTransport}>
            <div className={styles.wrapperImage}>
              <img src={AddTransport} alt="addTransportImage" />
            </div>
            <div className={styles.wrapperInfo}>
              <span className={styles.infoTitle}>
                Вы можете получать компенсацию
                {' '}
                <br />
                {' '}
                за использование услуги личного транспорта
              </span>
              <span className={styles.additionalInformation}>
                А так же брать пассажиров и зарабатывать на поездках
              </span>
              <button onClick={goAdd} className={styles.addButton}>
                <AddIcon />
                Добавить транспорт
              </button>
            </div>
          </div>
        )}
    </>
  );
});

const ErrorBoundaryWrapper: FC = ({ children }) => (
  <ErrorBoundary fallback={null}>
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
