import { FC, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router';
import dayjs from 'dayjs';

import { useCheckinInfo, useTrip } from 'api/services/Trips/Trips.query';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { routes } from 'constants/routes.constants';
import { useAppStore } from 'stores/stores.context';
import { UUID } from 'utils/io-ts';
import { getWaypointName } from 'utils/trips/getWaypointName';
import Button from 'components/Button/Button';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';

import { ReactComponent as Time } from 'assets/icons/time.svg';
import { ReactComponent as ArrowRoute } from 'assets/icons/arrow-route.svg';

import styles from './TripFinish.module.scss';

const statusesAtWaypoint = [
  TRIP_STATUSES.DRIVER_ARRIVED,
  TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED,
  TRIP_STATUSES.ORDER_FINISHED,
];

const TripFinish: FC = () => {
  const { id } = useParams<{ id: UUID }>();

  if (!id) {
    throw new Error('Trip id is required');
  }

  const trip = useTrip(id).data;
  const checkinInfo = useCheckinInfo({ tripId: id }).data;

  const waypointCheckins = checkinInfo?.chekins.filter(checkin => statusesAtWaypoint.includes(checkin.status)) || [];

  const { mainLayoutStore, mapStore } = useAppStore();

  useEffect(() => {
    mainLayoutStore.floatingPanel?.current?.setHeight(window.innerHeight * 0.95);
  }, [mainLayoutStore]);

  const tripDuration = checkinInfo?.tripDuration
    ? checkinInfo.tripDuration.days * 24 * 60 + checkinInfo.tripDuration.hours * 60 + checkinInfo.tripDuration.minutes
    : '';

  const navigate = useNavigate();

  const finish = () => {
    mapStore.clear();
    navigate(routes.Home);
  };

  return (
    <div className={styles.tripFinish}>
      <div>
        <div className={styles.title}>Маршрут</div>

        <div className={styles.card}>
          <div>
            <div className={styles.cardTitle}>Общее время</div>
            <div className={styles.cardValue}>
              <Time />
              <div>
                {tripDuration}
                {' '}
                мин
              </div>
            </div>
          </div>

          <div>
            <div className={styles.cardTitle}>Общий пробег</div>
            <div className={styles.cardValue}>
              <ArrowRoute />
              <div>
                {trip.factDistance?.toFixed(2)}
                {' '}
                км
              </div>
            </div>
          </div>
        </div>

        <div className={styles.waypoints}>
          {trip.waypoints.map(waypoint => (
            <div key={waypoint.index} className={styles.waypoint}>
              <div className={styles.route}>
                <div className={styles.routeLine} />
                <WaypointIcon
                  index={waypoint.index}
                  checkinType={waypointCheckins[waypoint.index]?.type}
                  className={styles.waypointIcon}
                />
                <div className={styles.routeLine} />
              </div>
              <div className={styles.waypointInfo}>
                <div className={styles.waypointTitle}>{getWaypointName(waypoint.index, trip.waypoints.length)}</div>
                <div className={styles.waypointAddress}>{waypoint.fullAddress}</div>
                <div className={styles.waypointTimes}>
                  {waypointCheckins[waypoint.index]?.time && (
                    <div className={styles.waypointTime}>
                      {dayjs(waypointCheckins[waypoint.index]?.time).format('HH:mm DD.MM.YY')}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <Button
        color="primary"
        onClick={finish}
        className={styles.button}
        block
      >
        Ок
      </Button>
    </div>
  );
};

export default TripFinish;
