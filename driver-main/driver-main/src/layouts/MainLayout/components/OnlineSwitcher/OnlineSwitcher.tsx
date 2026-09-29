import { FC } from 'react';
import { Switch } from 'antd-mobile';
import { observer } from 'mobx-react';
import { DriverSpecialityTypes } from 'api/services/DispatcherRoom/DispatcherRoom.types';
import { useProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { useUpdateDriverStatus } from 'api/services/Trips/Trips.query';
import { useUpdateCargoDriverStatus } from 'api/services/TripsCargo/TripsCargo.query';
import { useAppStore } from 'stores/stores.context';
import styles from './OnlineSwitcher.module.scss';

const OnlineSwitcher: FC = observer(() => {
  const { online, driverSpeciality } = useProfile().data;

  const { mutate: setDriverStatus, isPending } = useUpdateDriverStatus();
  const { mutate: setCargoDriverStatus, isPending: isPendingCargo } = useUpdateCargoDriverStatus();

  const { mainLayoutStore } = useAppStore();

  const toggleOnline = () => {
    if (!online) {
      mainLayoutStore.askConfirmVehicle();
      return;
    }

    const toggle = driverSpeciality === DriverSpecialityTypes.Cargo ? setCargoDriverStatus : setDriverStatus;

    toggle({ state: false });
  };

  return (
    <div className={styles['online-switcher']}>
      <div>
        <div className={styles.title}>На линию</div>
        <div className={styles.subtitle}>
          {online ? 'Вы готовы брать заказы' : 'Вы не берете заказы'}
        </div>
      </div>

      <Switch
        checked={online}
        onChange={toggleOnline}
        loading={isPending || isPendingCargo}
        className={styles.switch}
      />
    </div>
  );
});

export default OnlineSwitcher;
