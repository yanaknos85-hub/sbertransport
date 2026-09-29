import { FC, useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';
import { Collapse, Divider } from 'antd-mobile';
import cn from 'classnames';
import dayjs from 'dayjs';

import {
  useChangeTripStatus, useCheckinInfo, useTrip
} from 'api/services/Trips/Trips.query';
import { UUID } from 'utils/io-ts';
import { getFullName } from 'utils/formattors/getFullName';
import { ignore } from 'utils';
import { getWaypointName } from 'utils/trips/getWaypointName';
import { routes } from 'constants/routes.constants';
import {
  CheckinType,
  finalTripStatuses,
  OPEN_NAVIGATOR_PARAM,
  TaxiClass, TRIP_STATUSES, TypeOfAction, typeOfActionTitles
} from 'constants/trips.constants';
import { useAppStore } from 'stores/stores.context';
import NavBar from 'components/NavBar';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import Avatar from 'components/Avatar/Avatar';
import Button from 'components/Button/Button';
import TripInfo from 'components/TripInfo';

import { ReactComponent as PhoneInCircle } from 'assets/icons/phoneInCircle.svg';
import { ReactComponent as Support } from 'assets/icons/support.svg';

import styles from './Trip.module.scss';

const statusesAtWaypoint = [
  TRIP_STATUSES.DRIVER_ARRIVED,
  TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED,
  TRIP_STATUSES.ORDER_FINISHED,
];

const getTimeTitle = (index: number) => {
  switch (index) {
    case 0: return 'подачи';
    default: return 'прибытия';
  }
};

const Trip: FC = () => {
  const { id } = useParams<{ id: UUID }>();
  const { pathname } = useLocation();

  if (!id) {
    throw new Error('Trip id is required');
  }

  const trip = useTrip(id).data;
  const checkinInfo = useCheckinInfo({ tripId: id }).data;

  const isFinalTrip = finalTripStatuses.includes(trip.status);
  const isTripInfo = pathname.includes(routes.TripInfo.replace(':id', ''));

  const waypointCheckins = checkinInfo?.chekins.filter(checkin => statusesAtWaypoint.includes(checkin.status)) || [];

  const navigate = useNavigate();

  const onBack = () => {
    if (isTripInfo) {
      navigate(-1);
    } else {
      navigate({
        pathname: routes.Home,
      }, {
        state: { keepOpen: true },
      });
    }
  };

  const { mainLayoutStore, mapStore } = useAppStore();

  useEffect(() => {
    mainLayoutStore.floatingPanel?.current?.setHeight(window.innerHeight * 0.95);
  }, [mainLayoutStore]);

  const { mutateAsync: changeStatus, isPending } = useChangeTripStatus();

  const [longitude, latitude] = mapStore.center;

  const handleStart = () => {
    changeStatus({
      tripId: trip.id,
      status: TRIP_STATUSES.DRIVER_ON_THE_WAY,
      checkinType: CheckinType.MANUAL,
      longitude,
      latitude,
      trip,
    })
      .then(() => navigate({ search: `${OPEN_NAVIGATOR_PARAM}=true` }))
      .catch(ignore);
  };

  return (
    <div>
      <NavBar onBack={onBack}>
        №
        {trip.humanReadableId}
      </NavBar>

      <div className={styles.content}>
        <div className={styles.title}>Детализация маршрута</div>

        {trip.waypoints.map(waypoint => (
          <div key={waypoint.index} className={styles.waypointContainer}>
            <div className={styles.waypoint}>
              <WaypointIcon
                index={waypoint.index}
                checkinType={CheckinType.AUTO}
                className={styles.waypointIcon}
              />
              <div>
                <div className={styles.waypointTitle}>
                  {getWaypointName(waypoint.index, trip.waypoints.length)}
                </div>
                <div className={styles.address}>
                  {waypoint.fullAddress}
                </div>
              </div>
            </div>
            <div className={styles.contact}>
              <Avatar size={32} />
              <div className={styles.contactCenterBlock}>
                {waypoint.passengers?.[0]?.type && (
                  <span
                    className={cn(styles.waypointType, {
                      [styles.unborading]: waypoint.passengers[0].type === TypeOfAction.UNBOARDING,
                    })}
                  >
                    {typeOfActionTitles[waypoint.passengers[0].type]}
                  </span>
                )}
                {waypoint.passengers?.[0] && (
                  <div className={styles.name}>
                    {getFullName(waypoint.passengers[0])}
                  </div>
                )}
              </div>
              <div>
                <a href={`tel:${waypoint.passengers?.[0]?.phone}`}>
                  <PhoneInCircle />
                </a>
              </div>
            </div>
            {waypointCheckins[waypoint.index] && (
              <>
                <Divider />
                <div className={styles.timeContainer}>
                  <div className={styles.infoTitle}>
                    Дата и время
                    {getTimeTitle(waypoint.index)}
                  </div>
                  <div className={styles.infoDesc}>
                    {dayjs(waypointCheckins[waypoint.index].time).format('DD.MM.YYYY HH:mm')}
                  </div>
                </div>
              </>
            )}
          </div>
        ))}

        {trip.taxiClass === TaxiClass.GROUP_TRANSFER && (
          <Collapse className={styles.info}>
            <Collapse.Panel
              key="info"
              title="Информация о поездке"
            >
              <TripInfo trip={trip} />
            </Collapse.Panel>
          </Collapse>
        )}

        {!isFinalTrip && (
          <div className={styles.actions}>
            <a href={`tel:${trip.dispatcher?.phone}`} className={styles.action}>
              <Support />
            </a>

            <Button
              fill="outline"
              className={styles.startAction}
              onClick={handleStart}
              loading={isPending}
            >
              Начать
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Trip;
