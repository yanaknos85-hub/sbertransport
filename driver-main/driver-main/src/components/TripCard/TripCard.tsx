import { FC } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { Divider, Space } from 'antd-mobile';
import dayjs from 'dayjs';
import cn from 'classnames';

import { useChangeTripStatus } from 'api/services/Trips/Trips.query';
import { BusynessTrip, PassTrip } from 'api/services/Trips/Trips.types';
import {
  CheckinType, finalTripStatuses, OPEN_NAVIGATOR_PARAM, TRIP_STATUSES
} from 'constants/trips.constants';
import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';
import { ignore } from 'utils';

import { ReactComponent as Clock } from 'assets/icons/clock.svg';
import { ReactComponent as CarFront } from 'assets/icons/car-front.svg';
import { ReactComponent as Route } from 'assets/icons/route.svg';
import { ReactComponent as Support } from 'assets/icons/support.svg';
import { ReactComponent as Info } from 'assets/icons/info.svg';
import { ReactComponent as StartPoint } from 'assets/icons/start-point.svg';
import Taxi from 'assets/images/taxi.png';

import styles from './TripCard.module.scss';

interface TripCardProps {
  trip: PassTrip | BusynessTrip;
  mapCenter: [number, number];
  className?: string;
  isBusynessTrip?: boolean;
}

const TripCard: FC<TripCardProps> = ({
  trip, mapCenter, className, isBusynessTrip,
}) => {
  const { mutateAsync: changeStatus, isPending } = useChangeTripStatus();

  const passTrip = trip as PassTrip;
  const busynessTrip = trip as BusynessTrip;

  const [longitude, latitude] = mapCenter;

  const { pathname } = useLocation();
  const navigate = useNavigate();

  const handleStart = () => {
    changeStatus({
      tripId: trip.id,
      status: TRIP_STATUSES.DRIVER_ON_THE_WAY,
      checkinType: CheckinType.MANUAL,
      longitude,
      latitude,
      trip: passTrip,
    })
      .then(() => navigate({ search: `${OPEN_NAVIGATOR_PARAM}=true` }))
      .catch(ignore);
  };

  const expectedTime
    = passTrip.expectedTime !== undefined && passTrip.expectedTime !== null ? dayjs.duration(passTrip.expectedTime, 's') : undefined;

  const isFinalTrip = finalTripStatuses.includes(passTrip.status);
  const isMyTrips = pathname.includes(routes.MyTrips);

  return (
    <div className={cn(styles.trip, [className])}>
      {!isBusynessTrip && (
        <div className={styles.header}>
          <div className={styles.headerItem}>
            <Clock />
            <span>
              {expectedTime
              && expectedTime.format(
                `${expectedTime.days() > 0 ? 'D д ' : ''}${expectedTime.hours() > 0 ? 'H ч ' : ''}m мин`
              )}
            </span>
          </div>
          <div className={styles.divider} />
          <div className={styles.headerItem}>
            <CarFront />
            <span>
              {passTrip.expectedDistance?.toFixed(1)}
              {' '}
              км
            </span>
          </div>
        </div>
      )}
      <div className={styles.block}>
        <Space
          justify="between"
          align="center"
          block
        >
          <div className={styles.tripId}>
            №
            {trip.humanReadableId}
          </div>
          <div>
            <img
              src={Taxi}
              alt="Транспорт"
              className={styles.taxi}
            />
          </div>
        </Space>
        <Space
          justify="between"
          block
          className={styles.timeBlock}
        >
          <div>
            Пассажирские
            <br />
            перевозки
          </div>
          <div>{dayjs(trip.expectedStartTime!).format('HH:mm DD.MM.YYYY')}</div>
        </Space>
      </div>
      <Divider className={styles.blockDivider} />
      <div className={styles.block}>
        {isBusynessTrip ? (
          <div>
            <div className={styles.startRoute}>
              <StartPoint />
              <div>
                <div className={styles.addressType}>Начало поездки</div>
                <div className={styles.address}>{busynessTrip.startAddress.name}</div>
              </div>
            </div>
            <Divider className={styles.blockDivider} />

            <Button
              block
              color="default"
              className={styles.availableBtn}
            >
              {`Доступно ${dayjs(trip.expectedStartTime!).format('DD.MM.YY')}`}
            </Button>

          </div>
        ) : (
          <>
            <div className={styles.route}>
              <div>
                <Route />
              </div>
              <div>
                <div>
                  <div className={styles.addressType}>Начало поездки</div>
                  <div className={styles.address}>{passTrip.waypoints?.[0]?.fullAddress}</div>
                  <div className={styles.addressType}>Конец поездки</div>
                  <div className={styles.address}>{passTrip.waypoints?.at(-1)?.fullAddress}</div>
                </div>
              </div>
            </div>
            <div className={styles.actions}>
              {!isFinalTrip && (
              <a href={`tel:${passTrip.dispatcher?.phone}`} className={styles.action}>
                <Support />
              </a>
              )}
              <Link
                to={(isMyTrips ? routes.TripInfo : routes.Trip).replace(':id', trip.id)}
                className={cn(styles.action, {
                  [styles.detailsAction]: isFinalTrip,
                })}
              >
                {isFinalTrip ? 'Детализация маршрута' : <Info />}
              </Link>
              {!isFinalTrip && (
              <Button
                fill="outline"
                className={styles.startAction}
                onClick={handleStart}
                loading={isPending}
              >
                Начать
              </Button>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default TripCard;
