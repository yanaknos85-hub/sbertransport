import { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { useContractor, useProfile, useVehicle } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { useUpdateDriverStatus } from 'api/services/Trips/Trips.query';
import { useUpdateCargoDriverStatus } from 'api/services/TripsCargo/TripsCargo.query';
import { DriverSpecialityTypes } from 'api/services/DispatcherRoom/DispatcherRoom.types';
import { logger } from 'utils/logger/logger';
import { useAppStore } from 'stores/stores.context';
import Button from 'components/Button/Button';
import Spin from 'components/Spin';
import Genesis from 'assets/images/genesis.png';
import styles from './VehicleConfirm.module.scss';

const VehicleConfirm: FC = observer(() => {
  const {
    data: vehicle, error, isLoading, isError, isFetching,
  } = useVehicle({ retry: 0 });

  const { driverSpeciality, contractorId } = useProfile().data;

  const contactor = useContractor(contractorId).data;

  const { mainLayoutStore } = useAppStore();

  const { mutateAsync: setDriverStatus, isPending } = useUpdateDriverStatus();
  const { mutateAsync: setCargoDriverStatus, isPending: isPendingCargo } = useUpdateCargoDriverStatus();

  const toggleOnline = () => {
    const toggle = driverSpeciality === DriverSpecialityTypes.Cargo ? setCargoDriverStatus : setDriverStatus;

    toggle({ state: true })
      .then(mainLayoutStore.confirmVehicle)
      .catch();
  };

  useEffect(() => {
    mainLayoutStore.setMiddleAnchor(380);
    mainLayoutStore.floatingPanel?.current?.setHeight(380);
    mainLayoutStore.disableClosingPanel();

    return () => {
      mainLayoutStore.enableClosingPanel();
    };
  }, [mainLayoutStore, mainLayoutStore.floatingPanel]);

  useEffect(() => {
    if (error?.response?.status === 409 && !isLoading && !isFetching) {
      logger('fail', 'Невозможно выйти на линию. Свяжитесь с Диспетчером для создания смены');
      setTimeout(() => mainLayoutStore.confirmVehicle());
    }
  }, [error, isFetching, isLoading, mainLayoutStore]);

  if (isLoading || isError) {
    return <Spin />;
  }

  return (
    <div className={styles.vehicleConfirm}>
      <div className={styles.title}>Ваша машина</div>
      <div className={styles.subtitle}>За вами закреплена:</div>
      <div className={styles.content}>
        <img
          src={Genesis}
          alt="Автомобиль"
          className={styles.img}
        />
        <div className={styles.name}>
          {[vehicle?.model.brand, vehicle?.model.name].filter(Boolean).join(' ')}
        </div>
        <div className={styles.number}>{vehicle?.stateNumber}</div>
        <Button
          className={styles.confirmButton}
          onClick={toggleOnline}
          loading={isPending || isPendingCargo}
        >
          Подтвердить
        </Button>
        <a href={`tel:${contactor?.mainDispatcher?.phone}`} className={styles.textButton}>
          Связаться с диспетчером
        </a>
      </div>
    </div>
  );
});

export default VehicleConfirm;
