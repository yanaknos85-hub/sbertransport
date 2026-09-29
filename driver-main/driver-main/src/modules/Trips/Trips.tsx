import { FC, useEffect } from 'react';
import { useLocation } from 'react-router';
import dayjs from 'dayjs';
import { observer } from 'mobx-react';

import { useBusyness, useMyTrips } from 'api/services/Trips/Trips.query';
import { TRIP_STATUSES } from 'constants/trips.constants';
import TripCard from 'components/TripCard';
import Empty from 'components/Empty';
import { useAppStore } from 'stores/stores.context';

import styles from './Trips.module.scss';

// Интервал обновления поездок, пока нет вебсокетов
const REFETCH_INTERVAL = 60 * 1000;
const BUSYNESS_START_TIME = dayjs().toISOString();
const BUSYNESS_END_TIME = dayjs().add(1, 'w').toISOString();

const Trips: FC = observer(() => {
  const { mainLayoutStore, mapStore } = useAppStore();

  const locationState = useLocation().state;

  useEffect(() => {
    if (!locationState?.keepOpen) {
      mainLayoutStore.floatingPanel?.current?.setHeight(65);
    }
    mainLayoutStore.setMiddleAnchor(window.innerHeight * 0.95);
    mainLayoutStore.setBottomAnchor(65);
  }, [mainLayoutStore, locationState?.keepOpen]);

  const { content, totalElements } = useMyTrips(
    {
      statuses: [TRIP_STATUSES.DRIVER_ASSIGNED],
    },
    {
      refetchOnWindowFocus: true,
    }
  ).data;

  const data = useBusyness(
    {
      startTime: BUSYNESS_START_TIME,
      endTime: BUSYNESS_END_TIME,
    },
    {
      refetchInterval: REFETCH_INTERVAL,
      refetchOnWindowFocus: true,
    }
  ).data;

  const busynessTrips = data?.busyness[0] ? data.busyness[0].trips : null;

  const total = (totalElements || 0) + (busynessTrips?.length || 0);

  return (
    <>
      <div className={styles.title}>
        <span>Мои заказы </span>
        <span className={styles.quantity}>
          (
          {total}
          )
        </span>
      </div>
      <div className={styles.tripList}>
        {!content.length && !busynessTrips?.length ? (
          <Empty
            title="У вас еще нет заказов."
            description="Ожидайте назначения на Вас заказов диспетчером."
            className={styles.emptyList}
          />
        ) : (
          <>
            {content?.map(trip => (
              <TripCard
                key={trip.id}
                trip={trip}
                mapCenter={mapStore.center}
                className={styles.trip}
              />
            ))}
            {busynessTrips?.map(trip => (
              <TripCard
                isBusynessTrip
                key={trip.id}
                trip={trip}
                mapCenter={mapStore.center}
                className={styles.trip}
              />
            ))}
          </>
        )}

      </div>
    </>
  );
});

export default Trips;
