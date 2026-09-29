import { FC, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { FloatingPanel, FloatingPanelRef } from 'antd-mobile';
import cn from 'classnames';
import dayjs from 'dayjs';
import { useCheckinInfo } from 'api/services/Trips/Trips.query';
import { PassTrip, Waypoint as IWaypoint } from 'api/services/Trips/Trips.types';
import Checkbox from 'components/Checkbox/Checkbox';
import { CheckinType, TRIP_STATUSES } from 'constants/trips.constants';
import { getWaypointName } from 'utils/trips/getWaypointName';
import styles from './RoutePanel.module.scss';

const checkinTypes: Record<CheckinType, string> = {
  [CheckinType.MANUAL]: 'Геопозиция не подтверждена',
  [CheckinType.AUTO]: 'Геопозиция подтверждена',
};

interface WaypointProps {
  waypoint: IWaypoint;
  length: number;
  checkinType?: CheckinType;
  time?: string;
}

const Waypoint: FC<WaypointProps> = ({
  waypoint,
  length,
  checkinType,
  time,
}) => {
  return (
    <div className={styles.waypoint}>
      <Checkbox
        className={cn(styles.checkbox, { [styles.manual]: checkinType === CheckinType.MANUAL })}
        checked={!!checkinType}
        disabled
      />
      <div className={styles.waypointInfo}>
        <div className={styles.waypointName}>{getWaypointName(waypoint.index, length)}</div>
        <div className={styles.address}>{waypoint.fullAddress}</div>
        <div
          className={cn(styles.checkin, {
            [styles.auto]: checkinType === CheckinType.AUTO,
            [styles.manual]: checkinType === CheckinType.MANUAL,
          })}
        >
          {checkinType && checkinTypes[checkinType]}
        </div>
      </div>
      <div className={styles.time}>{time && dayjs(time).format('HH:mm')}</div>
    </div>
  );
};

const acnhors = [40, window.innerHeight * 0.95];

const statusesAtWaypoint = [
  TRIP_STATUSES.DRIVER_ARRIVED,
  TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED,
  TRIP_STATUSES.ORDER_FINISHED,
];

interface RoutePanelProps {
  close: () => void;
  trip: PassTrip;
}

const RoutePanel: FC<RoutePanelProps> = ({ close, trip }) => {
  const { data: checkinInfo } = useCheckinInfo({ tripId: trip.id });

  const waypointCheckins = checkinInfo?.chekins.filter(checkin => statusesAtWaypoint.includes(checkin.status)) || [];

  const ref = useRef<FloatingPanelRef>(null);

  useEffect(() => {
    if (ref.current) {
      ref.current.setHeight(acnhors.at(-1)!);
    }
  }, [ref]);

  const onHeightChange = (height: number) => {
    if (height <= acnhors[0]) {
      close();
    }
  };

  return createPortal(
    <FloatingPanel
      anchors={acnhors}
      ref={ref}
      onHeightChange={onHeightChange}
    >
      <div className={styles.routePanel}>
        <div className={styles.title}>Маршрут</div>

        {trip.waypoints.map(waypoint => (
          <Waypoint
            key={waypoint.index}
            waypoint={waypoint}
            length={trip.waypoints.length}
            checkinType={waypointCheckins[waypoint.index]?.type}
            time={waypointCheckins[waypoint.index]?.time}
          />
        ))}
      </div>
    </FloatingPanel>,
    document.getElementById('root')!
  );
};

export default RoutePanel;
