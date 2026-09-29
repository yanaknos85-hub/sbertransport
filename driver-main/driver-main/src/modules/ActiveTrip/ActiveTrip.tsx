import { FC, useEffect, useState } from 'react';
import { observer } from 'mobx-react';
import { Divider } from 'antd-mobile';
import cn from 'classnames';
import { useNavigate } from 'react-router';

import { PassTrip } from 'api/services/Trips/Trips.types';
import { useChangeTripStatus } from 'api/services/Trips/Trips.query';

import { useAppStore } from 'stores/stores.context';
import {
  CheckinType, OPEN_NAVIGATOR_PARAM, TRIP_STATUSES, TypeOfAction, typeOfActionTitles
} from 'constants/trips.constants';
import { routes } from 'constants/routes.constants';
import { getFullName } from 'utils/formattors/getFullName';
import { getDistanceBetweenPoints } from 'utils/trips/getDistanceBetweenPoints';
import Button from 'components/Button/Button';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import useUrl from 'hooks/useUrl';

import { useMap } from './hooks/useMap';
import { useNextWaypoint } from './hooks/useNextWaypoint';
import RouteButton from './components/RouteButton';
import RoutePanel from './components/RoutePanel';
import ConfirmPositionModal from './components/ConfirmPositionModal';
import Action from './components/Action';
import Timer from './components/Timer';
import Navigator from './components/Navigator';
import Detailed from './components/Detailed';

import NoPhoto from 'assets/images/no-photo.png';
import { ReactComponent as Phone } from 'assets/icons/phone2.svg';
import { ReactComponent as Triangle } from 'assets/icons/triangle.svg';
import { ReactComponent as Info } from 'assets/icons/info.svg';
import { ReactComponent as TwoGis } from 'assets/icons/2gis.svg';

import styles from './ActiveTrip.module.scss';

const nextStatusText = {
  [TRIP_STATUSES.DRIVER_ARRIVED]: 'Я на месте',
  [TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED]: 'Я на месте',
  [TRIP_STATUSES.TRIP_IN_PROGRESS]: 'Поехали',
  [TRIP_STATUSES.ORDER_FINISHED]: 'Завершить',
};

const MAX_AUTOCHECKIN_DISTANCE = 0.5;

const ActiveTripContent: FC<{ trip: PassTrip }> = observer(({ trip }) => {
  const { [OPEN_NAVIGATOR_PARAM]: openNavigator } = useUrl();

  const [isNavigator, setIsNavigator] = useState(!!openNavigator);
  const [isDetailedOpened, setIsDetailedOpened] = useState(false);
  const [isComfirmPositionOpened, setIsComfirmPositionOpened] = useState(false);

  useEffect(() => {
    if (openNavigator && trip.status === TRIP_STATUSES.DRIVER_ON_THE_WAY) {
      setIsNavigator(true);
    }
  }, [openNavigator, trip.status]);

  const { mapStore } = useAppStore();

  const {
    expectedTime, expectedDistance, expectedArrivalTime,
  } = useMap(trip);

  const { nextStatus, nextWaypointIndex } = useNextWaypoint(trip);

  const { mutateAsync: changeStatus, isPending } = useChangeTripStatus();

  const navigate = useNavigate();

  const confirmChangeStatus = (checkinType: CheckinType) => {
    if (!nextStatus || nextWaypointIndex === null) return;

    changeStatus({
      tripId: trip.id,
      status: nextStatus,
      longitude: trip.waypoints[nextWaypointIndex]?.longitude,
      latitude: trip.waypoints[nextWaypointIndex]?.latitude,
      checkinType,
    })
      .then(() => {
        if (nextStatus === TRIP_STATUSES.ORDER_FINISHED) {
          navigate(routes.TripFinish.replace(':id', trip.id));
        } else if (nextStatus === TRIP_STATUSES.TRIP_IN_PROGRESS) {
          setIsNavigator(true);
        }
      })
      .catch();
  };

  const onChangeStatus = () => {
    if (!nextStatus || nextWaypointIndex === null) return;

    const distance = getDistanceBetweenPoints(
      mapStore.center,
      [trip.waypoints[nextWaypointIndex]?.longitude, trip.waypoints[nextWaypointIndex]?.latitude]
    );

    const isAutoCheckin = nextStatus === TRIP_STATUSES.TRIP_IN_PROGRESS
      || (!mapStore.errorWatchingGeo && distance <= MAX_AUTOCHECKIN_DISTANCE);

    if (!isAutoCheckin) {
      setIsComfirmPositionOpened(true);
      return;
    }

    confirmChangeStatus(CheckinType.AUTO);
  };

  const pass = nextWaypointIndex !== null
    ? trip.waypoints[nextWaypointIndex]?.passengers?.[0]
    : null;

  const { mainLayoutStore } = useAppStore();

  useEffect(() => {
    if (isDetailedOpened) {
      mainLayoutStore.setMiddleAnchor(window.innerHeight * 0.95);
      mainLayoutStore.floatingPanel?.current?.setHeight(window.innerHeight * 0.95);
    } else {
      mainLayoutStore.setMiddleAnchor(495);
      mainLayoutStore.floatingPanel?.current?.setHeight(495);
    }
    mainLayoutStore.setBottomAnchor(75);
  }, [mainLayoutStore, mainLayoutStore.floatingPanel, isDetailedOpened]);

  if (isNavigator) return (
    <Navigator
      close={() => setIsNavigator(false)}
      longitude={nextWaypointIndex !== null ? trip.waypoints[nextWaypointIndex]?.longitude : undefined}
      latitude={nextWaypointIndex !== null ? trip.waypoints[nextWaypointIndex]?.latitude : undefined}
    />
  );

  if (isDetailedOpened) return <Detailed trip={trip} close={() => setIsDetailedOpened(false)} />;

  return (
    <>
      <div className={styles.routeInfo}>
        <div>
          <div>
            <div className={styles.title}>{expectedTime ? Math.round(expectedTime / 1000 / 60) : ''}</div>
            <div className={styles.subtitle}>мин</div>
          </div>
        </div>

        <div>
          <div>
            <div className={styles.title}>{expectedArrivalTime ? expectedArrivalTime?.format('HH:mm') : ''}</div>
            <div className={styles.subtitle}>прибытие</div>
          </div>
        </div>

        <div>
          <div>
            <div className={styles.title}>{expectedDistance?.toFixed(2)}</div>
            <div className={styles.subtitle}>км</div>
          </div>
        </div>
      </div>

      <div className={styles.activeTrip}>
        <div className={styles.addressTitle}>Следуйте к адресу</div>
        {nextWaypointIndex !== null && (
          <>
            <div className={styles.address}>
              <WaypointIcon
                index={nextWaypointIndex}
                checkinType={CheckinType.AUTO}
                className={styles.waypointIcon}
              />
              <span>{trip.waypoints[nextWaypointIndex]?.fullAddress}</span>
            </div>

            <div className={styles.contact}>
              <img src={NoPhoto} alt="Аватар" />
              <div className={styles.pass}>
                {pass?.type && (
                  <span
                    className={cn(styles.waypointType, {
                      [styles.unborading]: pass.type === TypeOfAction.UNBOARDING,
                    })}
                  >
                    {typeOfActionTitles[pass.type]}
                  </span>
                )}
                <div className={styles.name}>{getFullName(pass ?? {})}</div>
              </div>
              {nextStatus === TRIP_STATUSES.TRIP_IN_PROGRESS && (
                <div>
                  <div className={styles.timerTitle}>Ожидание</div>
                  <Timer />
                </div>
              )}
            </div>

            <div className={styles.navigatorLink} onClick={() => setIsNavigator(true)}>
              открыть в другом навигаторе
              <TwoGis />
            </div>
          </>
        )}

        <Divider />

        <div className={styles.actions}>
          <Action
            img={<Phone />}
            title="Звонок Клиенту"
            tel={pass?.phone}
          />
          <Action
            img={<Triangle />}
            title="Помощь диспетчера"
            tel={trip.dispatcher?.phone}
          />
          <Action
            img={<Info />}
            title="Детали поездки"
            onClick={() => setIsDetailedOpened(true)}
          />
        </div>

        {nextStatus && (
          <Button
            block
            onClick={onChangeStatus}
            loading={isPending}
            className={styles.statusButton}
          >
            {nextStatusText[nextStatus]}
          </Button>
        )}
      </div>

      <ConfirmPositionModal
        visible={isComfirmPositionOpened}
        onClose={() => setIsComfirmPositionOpened(false)}
        confirm={confirmChangeStatus}
      />
    </>
  );
});

const ActiveTrip: FC<{ trip: PassTrip }> = ({ trip }) => {
  const [isRouteOpened, setIsRouteOpened] = useState(false);

  return (
    <>
      <RouteButton open={() => setIsRouteOpened(true)} />
      {isRouteOpened && (
        <RoutePanel
          close={() => setIsRouteOpened(false)}
          trip={trip}
        />
      )}

      <ActiveTripContent trip={trip} />
    </>
  );
};

export default ActiveTrip;
